package me.jjeda.houseserver.portfolios;

import lombok.AllArgsConstructor;
import me.jjeda.houseserver.accounts.Account;
import me.jjeda.houseserver.accounts.CurrentUser;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity createPortfolio(@RequestBody @Valid  Portfolio requestPortfolio,
                                          @CurrentUser Account currentUser) {

        Portfolio portfolio = modelMapper.map(requestPortfolio, Portfolio.class);
        portfolio.setManager(currentUser);

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
                                          PagedResourcesAssembler<Portfolio> assembler,
                                          @CurrentUser Account account) {
        Page<Portfolio> page = this.portfolioRepository.findAll(pageable);
        PagedResources pagedResources = assembler.toResource(page, e -> new PortfolioResource(e));
        pagedResources.add(new Link("/docs/index.html#resources-portfolios-list").withRel("profile"));
        if (account != null) {
            pagedResources.add(linkTo(PortfolioController.class).withRel("create-portfolio"));
        }

        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getPortfolios(@PathVariable Integer id,
                                        @CurrentUser Account currentUser) {

        Optional<Portfolio> optionalPortfolio = this.portfolioRepository.findById(id);

        if (optionalPortfolio.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Portfolio portfolio = optionalPortfolio.get();
        PortfolioResource portfolioResource = new PortfolioResource(portfolio);
        portfolioResource.add(new Link("/docs/index.html#resources-portfolios-get").withRel("profile"));
        if(portfolio.getManager().equals(currentUser)) {
            portfolioResource.add(linkTo(PortfolioController.class).slash(portfolio.getId()).withRel("update-portfolio"));
        }
        return ResponseEntity.ok(portfolioResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updatePortfolios(@PathVariable Integer id,
                                           @RequestBody @Valid Portfolio portfolio,
                                           @CurrentUser Account currentUser) {
        Optional<Portfolio> optionalPortfolio = this.portfolioRepository.findById(id);
        if (optionalPortfolio.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Portfolio existingPortfolio = optionalPortfolio.get();
        if (!existingPortfolio.getManager().equals(currentUser)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

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
