package com.agileengine;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        validateParams(args);
        String sourceResourcePath = args[0];
        String searchResourcePath = args[1];
        String targetElementId = args[2];

        Optional<Element> buttonOpt = JsoupSearcher.findElementById(new File(sourceResourcePath), targetElementId);
        Optional<String> queryOpt = buttonOpt.map(Main::buildCssQuery);
        Optional<Elements> foundElements = queryOpt.flatMap(s -> JsoupSearcher.findElementsByQuery(new File(searchResourcePath), s));

        if (buttonOpt.isPresent()) {
            Optional<Element> element = foundElements.flatMap(elements -> findClosestElements(buttonOpt.get(), elements));
            element.ifPresent(el -> LOGGER.info("{}", PathBuilder.buildAbsolutePath(el)));
            element.ifPresent(el -> printSimilarAttributes(buttonOpt.get(), el));
        }
    }

    private static void printSimilarAttributes(Element sourceElement, Element foundElement) {
        List<String> similarAttributes = getSimilarAttributes(sourceElement, foundElement);
        similarAttributes.stream().sorted().forEach(el -> LOGGER.info("{}", el));
    }


    private static void validateParams(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("Please provide <input_origin_file_path> <input_other_sample_file_path> <targetElementId>");
        }
    }

    //query for search by single attribute, e.g. class="class1 class2" -> class=class1 class=class2
    private static String buildCssQuery(Element button) {
        List<String> selectors = new ArrayList<>();
        for (Attribute attr : button.attributes()) {
            List<String> values = Arrays.asList(attr.getValue().split(" "));
            String query = values.stream().map(x -> String.format("[%s~=%s]", attr.getKey(), x))
                    .collect(Collectors.joining(","));
            selectors.add(query);
        }
        return StringUtil.join(selectors, ",");
    }

    //closest mean max number of general attributes
    private static Optional<Element> findClosestElements(Element sourceElement, Elements elements) {
        Optional<Element> max = elements.stream().max((o1, o2) -> {
            int commonAttributes = getSimilarAttributes(sourceElement, o1).size();
            int commonAttributes2 = getSimilarAttributes(sourceElement, o2).size();
            return Integer.compare(commonAttributes, commonAttributes2);
        });
        return max;
    }

    private static List<String> getSimilarAttributes(Element sourceElement, Element targetElement) {
        Set<String> sourceAttributes = buildSingleKeyValueAttributes(sourceElement);
        Set<String> searchElements = buildSingleKeyValueAttributes(targetElement);
        return searchElements.stream().filter(sourceAttributes::contains).collect(Collectors.toList());
    }

    private static Set<String> buildSingleKeyValueAttributes(Element sourceElement) {
        Set<String> sourceAttributes = new HashSet<>();
        sourceAttributes.add("text=" + sourceElement.text());
        for (Attribute attribute : sourceElement.attributes()) {
            for (String attributeValue : attribute.getValue().split(" ")) {
                sourceAttributes.add((attribute.getKey() + "=" + attributeValue).toLowerCase());
            }
        }
        return sourceAttributes;
    }
}