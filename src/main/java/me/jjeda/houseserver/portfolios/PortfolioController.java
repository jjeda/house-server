package me.jjeda.houseserver.portfolios;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@AllArgsConstructor
@RequestMapping(value ="/api/portfolios" , produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class PortfolioController {

    private final PortfolioRepository portfolioRepository;

    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity createPortfolio(@RequestBody Portfolio requestPortfolio) {

        Portfolio portfolio =modelMapper.map(requestPortfolio, Portfolio.class);

        Portfolio newPortfolio = this.portfolioRepository.save(portfolio);
        ControllerLinkBuilder selfLinkBuilder = linkTo(PortfolioController.class).slash(newPortfolio.getId());
        URI createdUri = selfLinkBuilder.toUri();
        PortfolioResource portfolioResource = new PortfolioResource(portfolio);

        return ResponseEntity.created(createdUri).body(portfolioResource);
    }


}
