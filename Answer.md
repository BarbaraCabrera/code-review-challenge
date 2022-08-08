
# Reto: Servicio para gestión de calidad de los anuncios (Bárbara Cabrera)

## Meaningful names:

There are a number of variables whose names can be improved to be more descriptive:

1. When we define a generic method and add input parameters, as in the case of equals() which we define with an input
   parameter of type object, the parameter is better to be descriptive.
   That is, exemplifying it we see that currently it is described as:
~~~
    public boolean equals(Object o){}
~~~
and the ideal would be:
~~~
    public boolean equals(Object object){}
~~~
that way along the method we will follow much easier the path of the variable.

2. The same happens when we declare variables along the code.
   When we want to use a list to collect the words used in the description:
~~~
     List<String> wds = Arrays.asList(description.split(" "));
~~~
The names used have to be not only descriptive but pronounceable and "wds" is not.
Instead, we can use longer variable names, an example could be use wordsUsed or wordsCount.

3. Another examples:
~~~
    List<PublicAd> result = new ArrayList<>();
    List<QualityAd> result = new ArrayList<>();
~~~
Result isn't descriptive because we are not doing mathematical calculations, so in PublicAd case
one option could be use publicAdsList and for the QualityAds something similar, QualityAdsList.

4.Same as before. We need descriptive names.
~~~
       public void calculateScores() {
            adRepository
               .findAllAds()
               .forEach(this::calculateScore);
       }
~~~
we are using a method called calculateScores() but we also have another called calculateScore(Ad ad) and
this could be really confusing if you don't see the final s. I would change the calculateScores() for Scores()
or something like that.

## Corrections:

1. When checking if the ad is complete to add some extra points:
~~~
        if (ad.isComplete()) {
            score = Constants.FORTY;
        }
~~~
The + is missing. Whe should use:
~~~
        if (ad.isComplete()) {
            score += Constants.FORTY;
        }
~~~
to add the 40pts instead to make the score equal to 40.

## Improvements/Refactor:

1. When analysing the description to add points to the score, we are assuming that the user has written the words
   "ático" and "céntrico" with an accent, so there could be ads with these characteristics that are being missed if
   they are spelled wrong. The same happens with the lower and uppercase.
   It would be easy to add a method that removes the accents and convert the uppercase into lower ones from the text
   before separating it into words for analysis.
~~~
    private String cleanerDescriptions(Optional<String> optionalDescription){

        String description = optionalDescription.get();
        description = Normalizer.normalize(description, Normalizer.Form.NFD); //Canonical decomposition
        description = description.replaceAll("[\\p{InCombiningDiacriticalMarks}]", ""); //Removes accents and all diacritics
            
        return description.toLowerCase(); //return the description value in lower case

    }
~~~
2. The Ad class uses two constructors, which differ in two variables,
   the simpler one could be called within the one with the larger arguments to save repeated lines of code.
   That is:
~~~
    public Ad(Integer id,
        Typology typology,
        String description,
        List<Picture> pictures,
        Integer houseSize,
        Integer gardenSize,
        Integer score,
        Date irrelevantSince) {
            Ad(Integer id,
                Typology typology,
                String description,
                List<Picture> pictures,
                Integer houseSize,
                Integer gardenSize);
            this.score = score;
            this.irrelevantSince = irrelevantSince;
        }
~~~
3. In addition, as an improvement, we could add a method for the PublicAd class whose task is to pass the
   values of the Ad to create the PublicAd. We do not eliminate code but we leave it much more organized.
~~~
   public PublicAd createPublicAd(Ad ad, publicAd){
      publicAd.setDescription(ad.getDescription());
      publicAd.setGardenSize(ad.getGardenSize());
      publicAd.setHouseSize(ad.getHouseSize());
      publicAd.setId(ad.getId());
      publicAd.setPictureUrls(ad.getPictures().stream().map(Picture::getUrl).collect(Collectors.toList()));
      publicAd.setTypology(ad.getTypology().name());

      return publicAd;
   }
~~~
We can do the same with QualityAd
~~~
   public QualityAd createQualityAd(Ad ad, QualityAd qualityAd){
      qualityAd.setDescription(ad.getDescription());
      qualityAd.setGardenSize(ad.getGardenSize());
      qualityAd.setHouseSize(ad.getHouseSize());
      qualityAd.setId(ad.getId());
      qualityAd.setPictureUrls(ad.getPictures().stream().map(Picture::getUrl).collect(Collectors.toList()));
      qualityAd.setTypology(ad.getTypology().name());
      qualityAd.setScore(ad.getScore());
      qualityAd.setIrrelevantSince(ad.getIrrelevantSince());

      return qualityAd;
    }
~~~
and simplify both findPublicAds() and findQualityAds().
~~~
   public List<PublicAd> findPublicAds() {
      List<Ad> ads = adRepository.findRelevantAds();
      ads.sort(Comparator.comparing(Ad::getScore));

      List<PublicAd> publicAdsList = new ArrayList<>();
      for (Ad ad: ads) {
         PublicAd publicAd = new PublicAd();
         publicAdsList.add(publicAd.createPublicAd(ad, publicAd));
      }
      
      return publicAdsList;
      
    }

   public List<QualityAd> findQualityAds() {
      List<Ad> ads = adRepository.findIrrelevantAds();

      List<QualityAd> result = new ArrayList<>();
      for (Ad ad: ads) {
         QualityAd qualityAd = new QualityAd();
         qualityAdList.add(qualityAd.createQualityAd(ad, qualityAd));
      }
      
      return qualityAdList;
      
    }
~~~
## Comments:

1. Some comments like this are redundant:
~~~
    //Si no hay fotos restamos 10 puntos
    //Cada foto en alta definición aporta 20 puntos
    //Cada foto normal aporta 10 puntos
~~~
This is the easiest part of the code, to use comments those should help with the complex code, that clarify the return
of a function, for example:
~~~
    //Returns a sorted List with the relevant Ads of the repo that will be public for the user
    public List<PublicAd> findPublicAds() { .....
~~~
## Testing:

As for the tests, I have never worked with tests in this way (spring), but I understand that what we are doing is
creating a relevant and an irrelevant ad and then adding them to a repository. It calculates the mock score values but
after that I don't know exactly what it does, with the:
~~~
   verify(adRepository).findAllAds();
   verify(adRepository, times(2)).save(any());
~~~
I guess it verifies that both have been added.

Seeing this, I thought that we should at least check that, in ads where the score has not been added previously,
we calculate it correctly. And also, that the methods findPublicAds and findQualityAds return a list
with the correct ads.

