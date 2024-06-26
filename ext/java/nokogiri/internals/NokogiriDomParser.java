package nokogiri.internals;

import java.io.IOException;

import nokogiri.XmlDocument;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.XIncludeParserConfiguration;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.dtd.DTDConfiguration;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Sets up a Xerces/XNI DOM Parser for use with Nokogiri.  Uses
 * NekoDTD to parse the DTD into a tree of Nodes.
 *
 * @author Patrick Mahoney <pat@polycrystal.org>
 */
public class NokogiriDomParser extends DOMParser
{
  protected DOMParser dtd;
  protected boolean xInclude;
  protected XMLParserConfiguration config;

  public
  NokogiriDomParser(XMLParserConfiguration config)
  {
    super(config);
    this.config = config;
    initialize();
  }

  public
  NokogiriDomParser(ParserContext.Options options)
  {
    xInclude = options.xInclude;
    initialize();
  }

  protected void
  initialize()
  {
    if (config == null) {
      if (xInclude) {
        config = new XIncludeParserConfiguration();
      } else {
        config = getXMLParserConfiguration();
      }
    }

    DTDConfiguration dtdConfig = new DTDConfiguration();
    dtd = new DOMParser(dtdConfig);

    config.setDTDHandler(dtdConfig);
    config.setDTDContentModelHandler(dtdConfig);
  }

  @Override
  public void
  parse(InputSource source) throws SAXException, IOException
  {
    dtd.reset();
    if (xInclude) {
      setEntityResolver(new NokogiriXIncludeEntityResolver(source));
    }
    super.parse(source);
    Document doc = getDocument();
    if (doc == null) {
      throw new RuntimeException("null document");
    }

    doc.setUserData(XmlDocument.DTD_RAW_DOCUMENT, dtd.getDocument(), null);
  }

  private static class NokogiriXIncludeEntityResolver implements org.xml.sax.EntityResolver
  {
    InputSource source;
    private
    NokogiriXIncludeEntityResolver(InputSource source)
    {
      this.source = source;
    }

    @Override
    public InputSource
    resolveEntity(String publicId, String systemId)
    throws SAXException, IOException
    {
      if (systemId != null) { source.setSystemId(systemId); }
      if (publicId != null) { source.setPublicId(publicId); }
      return source;
    }
  }
}
