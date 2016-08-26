package ch.unil.fcrepo4.client;

import com.hp.hpl.jena.rdf.model.Property;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;

/**
 * Based on {@code org.fcrepo.kernel.api.RdfLexicon}.
 *
 * @author gushakov
 * @see <a href="https://github.com/fcrepo4/fcrepo4/blob/master/fcrepo-kernel-api/src/main/java/org/fcrepo/kernel/api/RdfLexicon.java">RdfLexicon.java</a>
 */
public class FcrepoConstants {
    public static final String FCR_METADATA = "fcr:metadata";

    public static final String FCR_TOMBSTONE = "fcr:tombstone";

    public static final String RDF_XML_MIMETYPE = "application/rdf+xml";

    // RDF lexicon, copied from org.fcrepo.kernel.api.RdfLexicon

    public static final String REPOSITORY_NAMESPACE =
            "http://fedora.info/definitions/v4/repository#";

    public static final String LDP_NAMESPACE = "http://www.w3.org/ns/ldp#";

    public static final Property CREATED_DATE =
            createProperty(REPOSITORY_NAMESPACE + "created");

    public static final Property CONTAINS =
            createProperty(LDP_NAMESPACE + "contains");

    public static final Property DC_TITLE =
            createProperty("http://purl.org/dc/elements/1.1/title");
}
