package me.jjeda.houseserver.portfolios;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@AllArgsConstructor
@RequestMapping(value = "/api/portfolios", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class PortfolioController {

    private final PortfolioRepository portfolioRepository;

    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity createPortfolio(@RequestBody Portfolio requestPortfolio) {

        Portfolio portfolio = modelMapper.map(requestPortfolio, Portfolio.class);

        Portfolio newPortfolio = this.portfolioRepository.save(portfolio);
        ControllerLinkBuilder selfLinkBuilder = linkTo(PortfolioController.class).slash(newPortfolio.getId());
        URI createdUri = selfLinkBuilder.toUri();
        PortfolioResource portfolioResource = new PortfolioResource(portfolio);

        return ResponseEntity.created(createdUri).body(portfolioResource);
    }

    @GetMapping
    public ResponseEntity queryPortfolios(Pageable pageable,
                                          PagedResourcesAssembler<Portfolio> assembler) {
        Page<Portfolio> page = this.portfolioRepository.findAll(pageable);
        PagedResources pagedResources = assembler.toResource(page, e -> new PortfolioResource(e));

        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getPortfolios(@PathVariable Integer id) {

        Optional<Portfolio> optionalPortfolio = this.portfolioRepository.findById(id);

        if (optionalPortfolio.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Portfolio portfolio = optionalPortfolio.get();
        PortfolioResource portfolioResource = new PortfolioResource(portfolio);
        return ResponseEntity.ok(portfolioResource);
    }

}
