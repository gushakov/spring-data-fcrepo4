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
     * Default datastream mimetype.
     */
    public static final String MIME_TYPE_TEXT_XML = "text/xml";

    /**
     * Default (test) URI. Must be declared in {@code fedora-node-types.cnd}.
     */
    public static final String TEST_FEDORA_URI_NAMESPACE = "info:fedora/test/";

    /**
     * Namespace prefix for default (test) namespace. Must be declared in {@code fedora-node-types.cnd}.
     */
    public static final String TEST_FEDORA_NS_PREFIX = "test";
}
