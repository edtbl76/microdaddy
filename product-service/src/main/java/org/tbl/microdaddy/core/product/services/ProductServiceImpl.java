package org.tbl.microdaddy.core.product.services;

import com.mongodb.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.tbl.microdaddy.api.core.product.Product;
import org.tbl.microdaddy.api.core.product.ProductService;
import org.tbl.microdaddy.api.exceptions.InvalidInputException;
import org.tbl.microdaddy.api.exceptions.NotFoundException;
import org.tbl.microdaddy.core.product.persistence.ProductEntity;
import org.tbl.microdaddy.core.product.persistence.ProductRepository;
import org.tbl.microdaddy.util.http.ServiceUtil;

@RestController
@Slf4j
public class ProductServiceImpl implements ProductService {


    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Product createProduct(Product body) {
       try {
           ProductEntity entity = mapper.apiToEntity(body);
           ProductEntity newEntity = repository.save(entity);

           log.debug("createProduct: entity created for productId: {}", body.getProductId());
           return mapper.entityToApi(newEntity);
       } catch (DuplicateKeyException ex) {
           throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
       }
    }

    @Override
    public Product getProduct(int productId) {
        log.debug("/product returns found product for productId={}", productId);

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        ProductEntity entity = repository.findByProductId(productId)
                        .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        log.debug("getProduct: found productId: {}", response.getProductId());
        return response;
    }

    @Override
    public void deleteProduct(int productId) {
        log.debug("deleteProduct: attempts to delete an entity with productId: {}", productId);
        repository.findByProductId(productId)
                .ifPresent(repository::delete);
    }
}
