package ch.unil.fcrepo4.spring.data.core;

/**
 * Constant values used throughout the module.
 * @author gushakov
 */
public class Constants {

    /**
     * Placeholder indicating the default (missing) value of the optional argument of an annotation.
     */
    public static final String DEFAULT_ANNOTATION_STRING_VALUE_TOKEN = "##default##";

    /**
     * Default (test) namespace, or root path of all Fedora objects.
     */
    public static final String DEFAULT_NAMESPACE = "test";

    /**
     * Default datastream mimetype.
     */
    public static final String MIME_TYPE_TEXT_XML = "text/xml";

    /**
     * Default (test) URI.
     */
    public static final String TEST_FEDORA_URI_NAMESPACE = "info:fedora/test/";
}
