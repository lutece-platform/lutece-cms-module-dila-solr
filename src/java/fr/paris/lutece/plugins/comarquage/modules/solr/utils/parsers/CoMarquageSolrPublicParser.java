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

import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;
import fr.paris.lutece.plugins.search.solr.util.SolrConstants;
import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.url.UrlItem;


/**
 * Parser for public cards (comarquage)
 */
public class CoMarquageSolrPublicParser extends DefaultHandler
{
    // -------------
    // - Constants -
    // -------------
    // Plugin name
    private static final String PROPERTY_PLUGIN_NAME = "comarquage.plugin.name";

    // CDC index keys
    private static final String PROPERTY_INDEXING_XML_BASE_VAR = "comarquage.path.xml";
    private static final String PROPERTY_INDEXING_FRAGMENT = "comarquage.indexing.";
    private static final String PROPERTY_LIST_CDC_INDEX_KEYS_FRAGMENT = "listCdcIndexKeys";

    // XPath comparisons
    private static final String PROPERTY_XPATH_CARD = "comarquage.parser.xpath.public.card";
    private static final String PROPERTY_XPATH_URL = "comarquage.parser.xpath.public.url";
    private static final String PROPERTY_XPATH_DATE = "comarquage.parser.xpath.public.date";
    private static final String PROPERTY_XPATH_TITLE = "comarquage.parser.xpath.public.title";
    private static final String PROPERTY_XPATH_THEME = "comarquage.parser.xpath.public.theme";
    private static final String PROPERTY_XPATH_KEYWORDS = "comarquage.parser.xpath.public.keywords";

    // Index type
    private static final String PROPERTY_INDEXING_TYPE = "comarquage-solr.indexing.publicType";

    // Site name
    private static final String PROPERTY_SITE = "lutece.name";
    private static final String PROPERTY_PROD_URL = "lutece.prod.url";

    // Paths contents
    private static final String PROPERTY_PATH_ID = "comarquage.parser.path.id";

    // URL delimiter
    private static final String PROPERTY_URL_DELIMITER = "comarquage.parser.url.public.delimiter";

    // Strings
    private static final String STRING_EMPTY = "";
    private static final String STRING_EQUAL = "=";
    private static final String STRING_SLASH = "/";
    private static final String STRING_SPACE = " ";
    private static final String SHORT_NAME = "comgepub";
    
    // -------------
    // - Variables -
    // -------------
    // List of Solr items
    private List<SolrItem> _listSolrItems;

    // XPath
    private String _strXPath;

    // Contents
    private String _strUrl;
    private String _strDate;
    private String _strType;
    private String _strTitle;
    private String _strSite;
    private String _strProdUrl;
    private String _strTheme;
    private String _strKeywords;

    /**
     * Initializes and launches the parsing of the public cards (public constructor)
     */
    public CoMarquageSolrPublicParser(  )
    {
        // Gets the list of CDC index keys
        String strCdcIndexKeys = AppPropertiesService.getProperty( PROPERTY_INDEXING_FRAGMENT +
                PROPERTY_LIST_CDC_INDEX_KEYS_FRAGMENT );

        // Initializes the Solr Item list
        _listSolrItems = new ArrayList<SolrItem>(  );

        // Initializes the indexing type
        _strType = AppPropertiesService.getProperty( PROPERTY_INDEXING_TYPE );

        // Initializes the site
        _strSite = AppPropertiesService.getProperty( PROPERTY_SITE );

        // Initializes the prod url
        _strProdUrl = AppPropertiesService.getProperty( PROPERTY_PROD_URL );

        if ( !_strProdUrl.endsWith( "/" ) )
        {
            _strProdUrl = _strProdUrl + "/";
        }

        try
        {
            // Initializes the SAX parser
            SAXParserFactory factory = SAXParserFactory.newInstance(  );
            SAXParser parser = factory.newSAXParser(  );

            // Splits the list of CDC index keys
            String[] splitKeys = strCdcIndexKeys.split( "," );

            for ( int i = 0; i < splitKeys.length; i++ )
            {
                // Gets the XML index file path
                String strXmlFile = AppPropertiesService.getProperty( PROPERTY_INDEXING_FRAGMENT + splitKeys[i] );
                String strXmlPath = AppPathService.getPath( PROPERTY_INDEXING_XML_BASE_VAR, strXmlFile );

                // Launches the parsing of this file (with the current handler)
                parser.parse( strXmlPath, this );
            }
        }
        catch ( ParserConfigurationException e )
        {
            AppLogService.error( e.getMessage(  ), e );
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
        _strUrl = STRING_EMPTY;
        _strDate = STRING_EMPTY;
        _strTitle = STRING_EMPTY;
        _strTheme = STRING_EMPTY;
        _strKeywords = STRING_EMPTY;
    }

    /**
     * Event received at the end of the parsing operation
     *
     * @throws SAXException any SAX exception
     */
    public void endDocument(  ) throws SAXException
    {
        // Nothing to do
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

        // Resets the contents
        String strXPathCard = AppPropertiesService.getProperty( PROPERTY_XPATH_CARD );

        if ( ( _strXPath != null ) && _strXPath.equals( strXPathCard ) )
        {
            _strUrl = STRING_EMPTY;
            _strTitle = STRING_EMPTY;
            _strTheme = STRING_EMPTY;
            _strKeywords = STRING_EMPTY;
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
        // If all the contents are retrieved (end of card)
        String strXPathCard = AppPropertiesService.getProperty( PROPERTY_XPATH_CARD );

        if ( ( _strXPath != null ) && _strXPath.equals( strXPathCard ) )
        {
            // Sets the path
            String strDelimiter = AppPropertiesService.getProperty( PROPERTY_URL_DELIMITER ) + STRING_EQUAL;
            String strPath = _strUrl.split( strDelimiter )[1];

            // Sets the full URL
            UrlItem url = new UrlItem( _strProdUrl );
            url.addParameter( XPageAppService.PARAM_XPAGE_APP, AppPropertiesService.getProperty( PROPERTY_PLUGIN_NAME ) );
            url.addParameter( AppPropertiesService.getProperty( PROPERTY_PATH_ID ), strPath );
            
            // Sets the contents
            String strContents = _strTitle + STRING_SPACE + _strKeywords + STRING_SPACE + _strTheme;

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

            // Sets the document fields
            // * FIELD_URL		: stored and indexed (without the analyser)
            // * FIELD_DATE		: stored and indexed (without the analyser)
            // * FIELD_UID		: stored and not indexed (the UID already exists in the URL)
            // * FIELD_CONTENTS	: not stored (saves disk space) and indexed (with the analyser)
            // * FIELD_TITLE	: stored and not indexed (the title already exists in the contents)
            // * FIELD_TYPE		: stored and indexed (without the analyser) -> allows to filter the search by type
            item.setUrl( url.getUrl(  ) );
            item.setDate( dateUpdate );
            item.setUid( strPath + SolrConstants.CONSTANT_UNDERSCORE + SHORT_NAME );
            item.setContent( strContents );
            item.setTitle( _strTitle );
            item.setType( _strType );
            item.setSite( _strSite );

            // Adds the new item to the map
            _listSolrItems.add( item );
        }

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
        String strXPathUrl = AppPropertiesService.getProperty( PROPERTY_XPATH_URL );
        String strXPathDate = AppPropertiesService.getProperty( PROPERTY_XPATH_DATE );
        String strXPathTitle = AppPropertiesService.getProperty( PROPERTY_XPATH_TITLE );
        String strXPathTheme = AppPropertiesService.getProperty( PROPERTY_XPATH_THEME );
        String strXPathKeywords = AppPropertiesService.getProperty( PROPERTY_XPATH_KEYWORDS );

        // Gets the URL
        if ( ( _strXPath != null ) && _strXPath.equals( strXPathUrl ) )
        {
            _strUrl += new String( ch, start, length );
        }

        // Gets the date
        else if ( ( _strXPath != null ) && _strXPath.equals( strXPathDate ) )
        {
            _strDate += new String( ch, start, length );
        }

        // Gets the title
        else if ( ( _strXPath != null ) && _strXPath.equals( strXPathTitle ) )
        {
            _strTitle += new String( ch, start, length );
        }

        // Gets the theme
        else if ( ( _strXPath != null ) && _strXPath.equals( strXPathTheme ) )
        {
            if ( ( _strTheme != null ) && !_strTheme.equals( STRING_EMPTY ) )
            {
                _strTheme += ( STRING_SPACE + new String( ch, start, length ) );
            }
            else
            {
                _strTheme += new String( ch, start, length );
            }
        }

        // Gets the keywords
        else if ( ( _strXPath != null ) && _strXPath.equals( strXPathKeywords ) )
        {
            _strKeywords += new String( ch, start, length );
        }
    }

    /**
     * Gets the list of Solr items
     *
     * @return The list of Solr items
     */
    public List<SolrItem> getPublicSolrItems(  )
    {
        return _listSolrItems;
    }
}
