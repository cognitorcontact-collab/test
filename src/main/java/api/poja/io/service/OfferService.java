package api.poja.io.service;

import static java.lang.Long.MAX_VALUE;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;

import api.poja.io.model.OfferDTOProjection;
import api.poja.io.model.OfferDto;
import api.poja.io.model.exception.NotFoundException;
import api.poja.io.repository.jpa.OfferJpaRepository;
import api.poja.io.service.subscription.SubscriptionConf;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OfferService {
  private static final String PREMIUM_OFFER_ID = "cb038529-dea0-43ab-b9bc-262ab668f150";

  private final OfferJpaRepository repository;
  private final UserService userService;
  private final SubscriptionConf subscriptionConf;

  public List<OfferDto> findAll() {
    return repository.findAllWithCount(now()).stream()
        .map(OfferDTOProjection::getOffer)
        .map(this::applyPremiumOfferMaxSubscribersNb)
        .toList();
  }

  public OfferDto getById(String id) {
    var offerDto =
        findById(id)
            .map(OfferDTOProjection::getOffer)
            .orElseThrow(() -> new NotFoundException("Offer.Id = " + id + " was not found."));
    return applyPremiumOfferMaxSubscribersNb(offerDto);
  }

  public Optional<OfferDTOProjection> findById(String id) {
    return repository.findByIdWithCount(now(), id);
  }

  public OfferDto getPremiumOffer() {
    return getById(PREMIUM_OFFER_ID);
  }

  public OfferDto getBasicOfferForUser(String userId) {
    var user = userService.getUserById(userId);
    if (user.isBetaTester()) {
      return OfferDto.builder()
          .id("basic")
          .name("basic")
          .maxApps(5)
          .maxSubscribers(MAX_VALUE)
          .priceInUsd(ZERO)
          .build();
    }

    if (user.isEndToEndTestUser()) {
      return OfferDto.builder()
          .id("basic")
          .name("basic")
          .maxApps(10)
          .maxSubscribers(MAX_VALUE)
          .priceInUsd(ZERO)
          .build();
    }

    return OfferDto.builder()
        .id("basic")
        .name("basic")
        .maxApps(2)
        .maxSubscribers(MAX_VALUE)
        .priceInUsd(ZERO)
        .build();
  }

  private OfferDto applyPremiumOfferMaxSubscribersNb(OfferDto offer) {
    if (PREMIUM_OFFER_ID.equals(offer.id())) {
      return offer.toBuilder().maxSubscribers(subscriptionConf.maxPremiumSubscribersNb()).build();
    }
    return offer;
  }
}
