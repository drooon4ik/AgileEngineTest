package com.agileengine;

import org.jsoup.nodes.Element;

import java.util.Optional;

public final class PathBuilder {

    private PathBuilder() {
    }

    public static String buildAbsolutePath(Element elem) {
        StringBuilder stringBuilder = new StringBuilder();
        buildAbsolutePath(elem, stringBuilder);
        return stringBuilder.toString();
    }

    private static void buildAbsolutePath(Element elem, StringBuilder sb) {
        Element parent = elem.parent();
        Optional<Element> parentOpt = Optional.ofNullable(parent);
        parentOpt.ifPresent(e -> buildAbsolutePath(e, sb));

        sb.append(" > ").append(elem.tagName());
        int index = elem.elementSiblingIndex();
        sb.append("[").append(index).append("]");
    }
}
