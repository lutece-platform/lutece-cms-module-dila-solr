/*
 * Copyright (c) 2002-2013, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.comarquage.modules.solr.utils.parsers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.paris.lutece.plugins.search.solr.indexer.SolrIndexerService;
import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;
import fr.paris.lutece.plugins.search.solr.util.SolrConstants;
import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.url.UrlItem;


/**
 * Parser for local cards (comarquage)
 */
public class CoMarquageSolrLocalParser extends DefaultHandler
{
    // -------------
    // - Constants -
    // -------------
    // Plugin name
    private static final String PROPERTY_PLUGIN_NAME = "comarquage.plugin.name";

    // Local cards path
    private static final String PROPERTY_INDEXING_LOCAL_PATH = "comarquage.indexing.localBasePath";
    private static final String PROPERTY_INDEXING_XML_BASE_VAR = "comarquage.path.xml";

    // XPath comparisons
    private static final String PROPERTY_XPATH_CARD = "comarquage.parser.xpath.local.card";
    private static final String PROPERTY_XPATH_DATE = "comarquage.parser.xpath.local.date";
    private static final String PROPERTY_XPATH_TITLE = "comarquage.parser.xpath.local.title";
    private static final String PROPERTY_ATTRIBUTE_URL = "comarquage.parser.xpath.local.attribute.url";

    // Index type
    private static final String PROPERTY_INDEXING_TYPE = "comarquage-solr.indexing.localType";

    // Path contents
    private static final String PROPERTY_PATH_ID = "comarquage.parser.path.id";
    private static final String PROPERTY_PATH_FIRST_NODE = "comarquage.parser.path.first.node";

    // URL delimiter
    private static final String PROPERTY_URL_DELIMITER = "comarquage.parser.url.local.delimiter";

    // Strings
    private static final String STRING_EMPTY = "";
    private static final String STRING_POINT = ".";
    private static final String STRING_SLASH = "/";
    private static final String STRING_SPACE = " ";
    private static final String SHORT_NAME = "comgeloc";

    // -------------
    // - Variables -
    // -------------
    // List of Solr items
    private List<SolrItem> _listSolrItems;

    // XPath
    private String _strXPath;

    // Contents
    private String _strURL;
    private String _strDate;
    private String _strType;
    private String _strSite;
    private String _strProdUrl;
    private String _strTitle;
    private String _strContents;

    /**
     * Initializes and launches the parsing of the local cards (public constructor)
     */
    public CoMarquageSolrLocalParser(  )
    {
        // Gets the local cards path
        String strLocalBasePath = AppPropertiesService.getProperty( PROPERTY_INDEXING_LOCAL_PATH );
        String strLocalPath = AppPathService.getPath( PROPERTY_INDEXING_XML_BASE_VAR, strLocalBasePath );
        File fileBasePath = new File( strLocalPath );

        // Initializes the SolrItem list
        _listSolrItems = new ArrayList<SolrItem>(  );

        // Initializes the indexing type
        _strType = AppPropertiesService.getProperty( PROPERTY_INDEXING_TYPE );

        // Initializes the site
        _strSite = SolrIndexerService.getWebAppName(  );

        // Initializes the prod url
        _strProdUrl = SolrIndexerService.getBaseUrl(  );

        if ( !_strProdUrl.endsWith( "/" ) )
        {
            _strProdUrl = _strProdUrl + "/";
        }

        try
        {
            // Initializes the SAX parser
            SAXParserFactory factory = SAXParserFactory.newInstance(  );
            SAXParser parser = factory.newSAXParser(  );

            // Launches the parsing on each local card
            parseAllLocalCards( fileBasePath, parser );
        }
        catch ( ParserConfigurationException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }
        catch ( SAXException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }
    }

    /**
     * Launches the parsing on each local card
     *
     * @param fileBasePath the base path
     * @param parser the SAX parser
     */
    private void parseAllLocalCards( File fileBasePath, SAXParser parser )
    {
        if ( fileBasePath.isFile(  ) )
        {
            // Launches the parsing of this local card (with the current handler)
            try
            {
                parser.parse( fileBasePath.getAbsolutePath(  ), this );
            }
            catch ( SAXException e )
            {
                AppLogService.error( e.getMessage(  ), e );
            }
            catch ( IOException e )
            {
                AppLogService.error( e.getMessage(  ), e );
            }
        }
        else
        {
            // Processes all the files of the current directory
            File[] files = fileBasePath.listFiles(  );

            for ( File fileCurrent : files )
            {
                if ( !fileCurrent.getAbsolutePath(  ).endsWith( "CVS" ) )
                {
                    // Launches the parsing on each local card (recursive)
                    parseAllLocalCards( fileCurrent, parser );
                }
            }
        }
    }

    /**
    * Event received when starting the parsing operation
    *
    * @throws SAXException any SAX exception
    */
    public void startDocument(  ) throws SAXException
    {
        // Initializes the XPATH
        _strXPath = STRING_EMPTY;

        // Initializes the contents
        _strURL = STRING_EMPTY;
        _strDate = STRING_EMPTY;
        _strTitle = STRING_EMPTY;
        _strContents = STRING_EMPTY;
    }

    /**
    * Event received at the end of the parsing operation
    *
    * @throws SAXException any SAX exception
    */
    public void endDocument(  ) throws SAXException
    {
        // Sets the ID 
        String strDelimiter = STRING_POINT + AppPropertiesService.getProperty( PROPERTY_URL_DELIMITER );
        String strFirstNode = AppPropertiesService.getProperty( PROPERTY_PATH_FIRST_NODE ) + STRING_SLASH;
        String strId = strFirstNode + _strURL.split( strDelimiter )[0];

        // Sets the full URL
        UrlItem url = new UrlItem( _strProdUrl );
        url.addParameter( XPageAppService.PARAM_XPAGE_APP, AppPropertiesService.getProperty( PROPERTY_PLUGIN_NAME ) );
        url.addParameter( AppPropertiesService.getProperty( PROPERTY_PATH_ID ), strId );
        
        // Converts the date from "dd MMMMM yyyy" to "yyyyMMdd"
        Locale locale = Locale.FRENCH;
        Date dateUpdate = null;

        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat( "dd MMMMM yyyy", locale );
            dateUpdate = dateFormat.parse( _strDate );

            dateFormat.applyPattern( "yyyyMMdd" );
        }
        catch ( ParseException e )
        {
            dateUpdate = null;
        }

        // Creates a new lucene document
        SolrItem item = new SolrItem(  );

        item.setUrl( url.getUrl(  ) );
        item.setDate( dateUpdate );
        item.setUid( strId + SolrConstants.CONSTANT_UNDERSCORE + SHORT_NAME );
        item.setContent( _strContents );
        item.setTitle( _strTitle );
        item.setType( _strType );
        item.setSite( _strSite );

        // Adds the new item to the list
        _listSolrItems.add( item );
    }

    /**
     * Event received at the start of an element
     *
     * @param uri the Namespace URI
     * @param localName the local name
     * @param qName the qualified XML name
     * @param atts the attributes attached to the element
     *
     * @throws SAXException any SAX exception
     */
    public void startElement( String uri, String localName, String qName, Attributes atts )
        throws SAXException
    {
        // Updates the XPath
        _strXPath += ( STRING_SLASH + qName );

        // Gets the URL (attribute)
        String strXPathCard = AppPropertiesService.getProperty( PROPERTY_XPATH_CARD );

        if ( ( _strXPath != null ) && _strXPath.equals( strXPathCard ) )
        {
            String strAttributeUrl = AppPropertiesService.getProperty( PROPERTY_ATTRIBUTE_URL );
            _strURL = atts.getValue( strAttributeUrl );
        }
    }

    /**
     * Event received at the end of an element
     *
     * @param uri the Namespace URI
     * @param localName the local name
     * @param qName the qualified XML name
     *
     * @throws SAXException any SAX exception
     */
    public void endElement( String uri, String localName, String qName )
        throws SAXException
    {
        // Updates the XPath
        _strXPath = _strXPath.substring( 0, _strXPath.lastIndexOf( STRING_SLASH ) );
    }

    /**
     * Event received when the analyzer encounters text (between two tags)
     *
     * @param ch the characters from the XML document
     * @param start the start position in the array
     * @param length the number of characters to read from the array
     *
     * @throws SAXException any SAX exception
     */
    public void characters( char[] ch, int start, int length )
        throws SAXException
    {
        // Gets the XPath comparisons properties
        String strXPathDate = AppPropertiesService.getProperty( PROPERTY_XPATH_DATE );
        String strXPathTitle = AppPropertiesService.getProperty( PROPERTY_XPATH_TITLE );

        // Gets the date
        if ( ( _strXPath != null ) && _strXPath.equals( strXPathDate ) )
        {
            _strDate += new String( ch, start, length );
        }

        // Gets the title
        else if ( ( _strXPath != null ) && _strXPath.equals( strXPathTitle ) )
        {
            _strTitle += new String( ch, start, length );
        }

        // Gets the contents
        if ( ( _strContents != null ) && !_strContents.equals( STRING_EMPTY ) )
        {
            _strContents += ( STRING_SPACE + new String( ch, start, length ) );
        }
        else
        {
            _strContents += new String( ch, start, length );
        }
    }

    /**
    * Gets the list of Solr items
    *
    * @return The list of Solr items
    */
    public List<SolrItem> getLocalSolrItems(  )
    {
        return _listSolrItems;
    }
}
