package com.myriadcode.languagelearner.flashcards_study.domain.views;

//    FIXME: for now this is built for language learning only. will modify it later to make it generic.
public record FlashCardView(String id,
                            Front front,
                            Back back,
                            String note,
                            boolean isReverse,
                            boolean isRevision) {
    public record Front(String text) {
    }

    public record Back(String text) {
    }
}
