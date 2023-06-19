#!/usr/bin/env bash
#
# Usage:
#
#   HOST=localhost PORT=7000 ./landscape-test.sh
#
: ${HOST=localhost}
: ${PORT=8080}
: ${PRODUCT_ID_OK=1}
: ${PRODUCT_ID_NOT_FOUND=13}
: ${PRODUCT_ID_NO_RECOMMENDATIONS=113}
: ${PRODUCT_ID_NO_REVIEWS=213}

function assertCurl() {

  local expectedHttpStatus=$1
  local curlCommand="$2 -w \"%{http_code}\""
  local result=$(eval $curlCommand)
  local httpStatus="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpStatus" = "$expectedHttpStatus" ]
  then
    if [ "$httpStatus" = "200" ]
    then
      echo "Test OK (HTTP Status: $httpStatus)"
    else
      echo "Test OK (HTTP Status: $httpStatus, $RESPONSE)"
    fi
  else
    echo "Test FAILED, EXPECTED HTTP Status: $expectedHttpStatus, GOT: $httpStatus, ABORTING!"
    echo "- Failing Command: $curlCommand"
    echo "- Response Body: $RESPONSE"
    exit 1
  fi
}

function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, ABORTING!"
    exit 1
  fi
}

function testUrl() {
  url=$@
  if curl $url -ks -f -o /dev/null
  then
    return 0
  else
    return 1
  fi;
}

function waitForService() {
  url=$@
  echo -n "Wait for: $url... "
  n=0
  until testUrl $url
  do
    n=$((n + 1))
    if [[ $n == 100 ]]
    then
      echo "Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "DONE, continues..."
}


function testCompositeCreated() {

  # Expects Product Composite for productId has been created w/ 3 recs and revs.
  if ! assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PRODUCT_ID_OK -s"
  then
    echo -n "FAIL"
    return 1
  fi

  set +e
  assertEqual "$PRODUCT_ID_OK" $(echo $RESPONSE | jq .productId)
  if [ "$?" -eq "1" ] ; then return 1; fi

  assertEqual 3  $(echo $RESPONSE | jq ".recommendations | length")
  if [ "$?" -eq "1" ] ; then return 1; fi

  assertEqual 3  $(echo $RESPONSE | jq ".reviews | length")
  if [ "$?" -eq "1" ] ; then return 1; fi

}

function waitForMessageProcessing() {
  echo "Waiting for messages to be processed..."

  # Give some time to complete
  sleep 1

  n=0
  until testCompositeCreated
  do
    n=$((n + 1))
    if [[ $n == 40 ]]
    then
      echo " Give up"
      exit 1
    else
      sleep 6
      echo -n ", retry #$n "
    fi
  done

  echo "All messages have been processed"
}

function recreateComposite() {
  local productId=$1
  local composite=$2

  assertCurl 202 "curl -X DELETE http://$HOST:$PORT/product-composite/${productId} -s"
  assertEqual 202 $(curl -X POST -s http://$HOST:$PORT/product-composite -H "Content-Type: application/json" --data "$composite" -w "%{http_code}")
}

function seedTestData() {
  body="{\"productId\":$PRODUCT_ID_NO_RECOMMENDATIONS"
  body+=\
',"name":"product name A","weight":100, "reviews":[
{"reviewId":1,"author":"author 1","subject":"subject 1","content":"content 1"},
{"reviewId":2,"author":"author 2","subject":"subject 2","content":"content 2"},
{"reviewId":3,"author":"author 3","subject":"subject 3","content":"content 3"}
]}'
  recreateComposite "$PRODUCT_ID_NO_RECOMMENDATIONS" "$body"

  body="{\"productId\":$PRODUCT_ID_NO_REVIEWS"
  body+=\
',"name":"product name B","weight":200, "recommendations":[
{"recommendationId":1,"author":"author 1","rate":1,"content":"content 1"},
{"recommendationId":2,"author":"author 2","rate":2,"content":"content 2"},
{"recommendationId":3,"author":"author 3","rate":3,"content":"content 3"}
]}'
  recreateComposite "$PRODUCT_ID_NO_REVIEWS" "$body"

    body="{\"productId\":$PRODUCT_ID_OK"
    body+=\
',"name":"product name C","weight":300, "recommendations":[
      {"recommendationId":1,"author":"author 1","rate":1,"content":"content 1"},
      {"recommendationId":2,"author":"author 2","rate":2,"content":"content 2"},
      {"recommendationId":3,"author":"author 3","rate":3,"content":"content 3"}
  ], "reviews":[
      {"reviewId":1,"author":"author 1","subject":"subject 1","content":"content 1"},
      {"reviewId":2,"author":"author 2","subject":"subject 2","content":"content 2"},
      {"reviewId":3,"author":"author 3","subject":"subject 3","content":"content 3"}
  ]}'
    recreateComposite "$PRODUCT_ID_OK" "$body"
}

set -e

echo "Starting Landscape Tests: " `date`

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
  echo "Restarting test environment..."
  echo "$ docker-compose down --remove-orphans"
  docker-compose down --remove-orphans
  echo "$docker-compose up -d"
  docker-compose up -d
fi

waitForService curl http://$HOST:${PORT}/actuator/health
seedTestData

waitForMessageProcessing

# Verify normal request. (3 recommendations, 3 reviews)
assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PRODUCT_ID_OK -s"
assertEqual $PRODUCT_ID_OK $(echo $RESPONSE | jq .productId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

# Verify 404 Not Found is returned for non-existing products
assertCurl 404 "curl http://$HOST:$PORT/product-composite/$PRODUCT_ID_NOT_FOUND -s"
assertEqual "No product found for productId: $PRODUCT_ID_NOT_FOUND" "$(echo $RESPONSE | jq -r .message)"

# Verify no recommendations are returned
assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PRODUCT_ID_NO_RECOMMENDATIONS -s"
assertEqual $PRODUCT_ID_NO_RECOMMENDATIONS $(echo $RESPONSE | jq .productId)
assertEqual 0 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

# Verify no reviews are returned
assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PRODUCT_ID_NO_REVIEWS -s"
assertEqual $PRODUCT_ID_NO_REVIEWS $(echo $RESPONSE | jq .productId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")

# Verify 422 (Unprocessable Entity) returned for a product Id that is out of range
assertCurl 422 "curl http://$HOST:$PORT/product-composite/-1 -s"
assertEqual "\"Invalid productId: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify 400 (Bad Request) returned for a product that isn't a number
assertCurl 400 "curl http://$HOST:$PORT/product-composite/invalidProductId -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

# Verify access to Swagger/OpenAPI URLs
echo "Swagger/OpenAPI tests"
assertCurl 302 "curl -s http://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -sL http://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/webjars/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/v3/api-docs"

assertEqual "3.0.1" "$(echo $RESPONSE | jq -r .openapi)"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/v3/api-docs.yaml"


if [[ $@ == *"stop"* ]]
then
  echo "Tests completed, shutting down test environment..."
  echo "$ docker-compose down"
  docker-compose down
fi

echo "End, all tests OK: " `date`



