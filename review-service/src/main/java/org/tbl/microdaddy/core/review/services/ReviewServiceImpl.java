package org.tbl.microdaddy.core.review.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.tbl.microdaddy.api.core.review.Review;
import org.tbl.microdaddy.api.core.review.ReviewService;
import org.tbl.microdaddy.api.exceptions.InvalidInputException;
import org.tbl.microdaddy.core.review.persistence.ReviewRepository;
import org.tbl.microdaddy.util.http.ServiceUtil;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;

    private final ReviewMapper mapper;

    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(
            ReviewRepository repository,
            ReviewMapper mapper,
            ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public List<Review> getReviews(int productId) {

        // TODO impl db to remove simulation code
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 213) {
            log.debug("No reviews found for product id: {}", productId);
            return new ArrayList<>();
        }

        List<Review> list = new ArrayList<>();

        list.add(new Review(
                productId,
                1,
                "Author 1",
                "Subject 1",
                "Content 1",
                serviceUtil.getServiceAddress()));
        list.add(new Review(
                productId,
                2,
                "Author 2",
                "Subject 2",
                "Content 2",
                serviceUtil.getServiceAddress()));
        list.add(new Review(
                productId,
                3,
                "Author 3",
                "Subject 3",
                "Content 3",
                serviceUtil.getServiceAddress()));

        log.debug("/reviews response size: {}",list.size());
        return list;
    }
}
