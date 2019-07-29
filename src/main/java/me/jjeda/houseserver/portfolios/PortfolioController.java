package me.jjeda.houseserver.portfolios;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@AllArgsConstructor
@RequestMapping(value = "/api/portfolios", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class PortfolioController {

    private final PortfolioRepository portfolioRepository;

    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity createPortfolio(@RequestBody @Valid  Portfolio requestPortfolio) {

        Portfolio portfolio = modelMapper.map(requestPortfolio, Portfolio.class);

        Portfolio newPortfolio = this.portfolioRepository.save(portfolio);
        ControllerLinkBuilder selfLinkBuilder = linkTo(PortfolioController.class).slash(newPortfolio.getId());
        URI createdUri = selfLinkBuilder.toUri();
        PortfolioResource portfolioResource = new PortfolioResource(portfolio);
        portfolioResource.add(linkTo(PortfolioController.class).withRel("query-portfolios"));
        portfolioResource.add(selfLinkBuilder.withRel("update-portfolio"));
        portfolioResource.add(new Link("/docs/index.html#resources-portfolios-create").withRel("profile"));

        return ResponseEntity.created(createdUri).body(portfolioResource);
    }

    @GetMapping
    public ResponseEntity queryPortfolios(Pageable pageable,
                                          PagedResourcesAssembler<Portfolio> assembler) {
        Page<Portfolio> page = this.portfolioRepository.findAll(pageable);
        PagedResources pagedResources = assembler.toResource(page, e -> new PortfolioResource(e));
        pagedResources.add(new Link("/docs/index.html#resources-portfolios-list").withRel("profile"));

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
        portfolioResource.add(new Link("/docs/index.html#resources-portfolios-get").withRel("profile"));
        return ResponseEntity.ok(portfolioResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updatePortfolios(@PathVariable Integer id,
                                           @RequestBody @Valid Portfolio portfolio) {
        Optional<Portfolio> optionalPortfolio = this.portfolioRepository.findById(id);
        if (optionalPortfolio.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Portfolio existingPortfolio = optionalPortfolio.get();
        this.modelMapper.map(portfolio,existingPortfolio);
        existingPortfolio.setModifiedDateTime(LocalDateTime.now());
        Portfolio savedPortfolio = this.portfolioRepository.save(existingPortfolio);

        PortfolioResource portfolioResource = new PortfolioResource(savedPortfolio);
        portfolioResource.add(new Link("/docs/index.html#resources-portfolios-update").withRel("profile"));
        return ResponseEntity.ok(portfolioResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deletePortfolio(@PathVariable Integer id) {
        this.portfolioRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}
