package org.tbl.microdaddy.core.product.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tbl.microdaddy.api.core.product.Product;
import org.tbl.microdaddy.core.product.persistence.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "serviceAddress", ignore = true)
    Product entityToApi(ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    ProductEntity apiToEntity(Product api);

}
