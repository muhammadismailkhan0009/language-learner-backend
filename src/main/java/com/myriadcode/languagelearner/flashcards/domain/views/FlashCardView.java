package com.myriadcode.languagelearner.flashcards.domain.views;

//    FIXME: for now this is built for language learning only. will modify it later to make it generic.
public record FlashCardView(String id,
                            Front front,
                            Back back,
                            boolean isReverse,
                            String relatedTo,
                            String scenario) {
    public record Front(String text) {
    }

    public record Back(String text) {
    }
}
