package com.idealista.application;

import com.idealista.domain.*;
import com.idealista.infrastructure.api.PublicAd;
import com.idealista.infrastructure.api.QualityAd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdsServiceImpl implements AdsService {

    @Autowired
    private AdRepository adRepository;

    @Override
    public List<PublicAd> findPublicAds() {
        List<Ad> ads = adRepository.findRelevantAds();
        ads.sort(Comparator.comparing(Ad::getScore));

        List<PublicAd> result = new ArrayList<>(); // result isn't descriptive - publicAdsList
        for (Ad ad: ads) {
            PublicAd publicAd = new PublicAd();
            publicAd.setDescription(ad.getDescription()); //*remove from here*
            publicAd.setGardenSize(ad.getGardenSize());
            publicAd.setHouseSize(ad.getHouseSize());
            publicAd.setId(ad.getId());
            publicAd.setPictureUrls(ad.getPictures().stream().map(Picture::getUrl).collect(Collectors.toList()));
                //obtains the URL from a list of pictures
            publicAd.setTypology(ad.getTypology().name()); //*remove to here*

            result.add(publicAd); //result.add(publicAd.createPublicAd(ad, publicAd));
        }
        return result;
    }

    @Override
    public List<QualityAd> findQualityAds() {
        List<Ad> ads = adRepository.findIrrelevantAds();

        List<QualityAd> result = new ArrayList<>(); //result isn't descriptive - QualityAdsList
        // method Create QAd
        for (Ad ad: ads) {
            QualityAd qualityAd = new QualityAd();
            qualityAd.setDescription(ad.getDescription());//*remove from here*
            qualityAd.setGardenSize(ad.getGardenSize());
            qualityAd.setHouseSize(ad.getHouseSize());
            qualityAd.setId(ad.getId());
            qualityAd.setPictureUrls(ad.getPictures().stream().map(Picture::getUrl).collect(Collectors.toList()));
            qualityAd.setTypology(ad.getTypology().name());
            qualityAd.setScore(ad.getScore());
            qualityAd.setIrrelevantSince(ad.getIrrelevantSince());//*remove to here*

            result.add(qualityAd); //result.add(qualityAd.createQualityAd(ad, qualityAd));
        }

        return result;
    }

    @Override
    public void calculateScores() {
        adRepository
                .findAllAds()
                .forEach(this::calculateScore);
    }

    private void calculateScore(Ad ad) {
        int score = Constants.ZERO;

        //Calcular puntuación por fotos
        if (ad.getPictures().isEmpty()) {
            score -= Constants.TEN; //Si no hay fotos restamos 10 puntos
        } else {
            for (Picture picture: ad.getPictures()) {
                if(Quality.HD.equals(picture.getQuality())) {
                    score += Constants.TWENTY; //Cada foto en alta definición aporta 20 puntos
                } else {
                    score += Constants.TEN; //Cada foto normal aporta 10 puntos
                }
            }
        }

        //Calcular puntuación por descripción
        Optional<String> optDesc = Optional.ofNullable(ad.getDescription()); //optionalDescription

        if (optDesc.isPresent()) {
            //String description = Utils.cleanerDescriptions(optDesc);
            String description = optDesc.get();

            if (!description.isEmpty()) {
                score += Constants.FIVE;
            }

            List<String> wds = Arrays.asList(description.split(" ")); //(número de palabras) wordsUsed/wordsCount
            if (Typology.FLAT.equals(ad.getTypology())) {
                if (wds.size() >= Constants.TWENTY && wds.size() <= Constants.FORTY_NINE) {
                   score += Constants.TEN;
                }

                if (wds.size() >= Constants.FIFTY) {
                    score += Constants.THIRTY;
                }
            }

            if (Typology.CHALET.equals(ad.getTypology())) {
                if (wds.size() >= Constants.FIFTY) {
                    score += Constants.TWENTY;
                }
            }
            //after create de Utils.cleanerDescriptions() we can ignore de accents and uppercase in the description
            if (wds.contains("luminoso")) score += Constants.FIVE;
            if (wds.contains("nuevo")) score += Constants.FIVE;
            if (wds.contains("céntrico")) score += Constants.FIVE; //centrico
            if (wds.contains("reformado")) score += Constants.FIVE;
            if (wds.contains("ático")) score += Constants.FIVE; //atico
        }

        //Calcular puntuación por completitud
        if (ad.isComplete()) {
            score = Constants.FORTY; //missing +
        }

        ad.setScore(score);

        if (ad.getScore() < Constants.ZERO) {
            ad.setScore(Constants.ZERO);
        }

        if (ad.getScore() > Constants.ONE_HUNDRED) {
            ad.setScore(Constants.ONE_HUNDRED);
        }

        if (ad.getScore() < Constants.FORTY) {
            ad.setIrrelevantSince(new Date());
        } else {
            ad.setIrrelevantSince(null);
        }

        adRepository.save(ad);
    }


}
