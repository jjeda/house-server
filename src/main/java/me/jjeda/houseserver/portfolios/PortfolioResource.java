package me.jjeda.houseserver.portfolios;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class PortfolioResource extends Resource<Portfolio> {

    public PortfolioResource(Portfolio portfolio, Link... links) {
        super(portfolio, links);
        add(linkTo(PortfolioController.class).slash(portfolio.getId()).withSelfRel());
    }
}
