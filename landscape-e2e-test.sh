#!/usr/bin/env bash
#
# Usage:
#
#   HOST=localhost PORT=7000 ./landscape-e2e-test.sh
#
: ${HOST=localhost}
: ${PORT=7000}
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

set -e

echo "HOST=${HOST}"
echo "PORT=${PORT}"

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

echo "End, all tests OK: " `date`



