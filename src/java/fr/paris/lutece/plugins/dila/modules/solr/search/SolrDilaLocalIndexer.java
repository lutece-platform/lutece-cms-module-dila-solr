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
package fr.paris.lutece.plugins.dila.modules.solr.search;

import fr.paris.lutece.plugins.dila.modules.solr.utils.parsers.DilaSolrLocalParser;
import fr.paris.lutece.plugins.search.solr.business.field.Field;
import fr.paris.lutece.plugins.search.solr.indexer.SolrIndexer;
import fr.paris.lutece.plugins.search.solr.indexer.SolrIndexerService;
import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.ArrayList;
import java.util.List;


/**
 * The Dila indexer for Solr search platform
 *
 */
public class SolrDilaLocalIndexer implements SolrIndexer
{
    private static final String PROPERTY_DESCRIPTION = "dila-solr.indexing.localIndexer.description";
    private static final String PROPERTY_NAME = "dila-solr.indexing.localIndexer.name";
    private static final String PROPERTY_VERSION = "dila-solr.indexing.localIndexer.version";
    private static final String PROPERTY_INDEXER_ENABLE = "dila-solr.indexing.localIndexer.enable";
    private static final String COM_INDEXATION_ERROR = "[SolrDilaLocalIndexer] An error occured during the indexation of a local element ";

    /**
     * {@inheritDoc}
     */
    public String getDescription(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_DESCRIPTION );
    }

    /**
     * {@inheritDoc}
     */
    public String getName(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_NAME );
    }

    /**
     * {@inheritDoc}
     */
    public String getVersion(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_VERSION );
    }

    /**
     * {@inheritDoc}
     */
    public List<String> indexDocuments(  )
    {
        // Parses the local cards
        DilaSolrLocalParser localParser = new DilaSolrLocalParser(  );

        // Gets the list of solr documents (to add to the index)
        List<SolrItem> listDocuments = localParser.getLocalSolrItems(  );

        List<String> lstErrors = new ArrayList<String>(  );

        for ( SolrItem solrItem : listDocuments )
        {
            try
            {
                SolrIndexerService.write( solrItem );
            }
            catch ( Exception e )
            {
                lstErrors.add( SolrIndexerService.buildErrorMessage( e ) );
                AppLogService.error( COM_INDEXATION_ERROR, e );
            }
        }

        return lstErrors;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnable(  )
    {
        return "true".equalsIgnoreCase( AppPropertiesService.getProperty( PROPERTY_INDEXER_ENABLE ) );
    }

    /**
     * {@inheritDoc}
     */
    public List<Field> getAdditionalFields(  )
    {
        // No additional fields for this indexer
        return new ArrayList<Field>(  );
    }

    /**
     * {@inheritDoc}
     */
    public List<SolrItem> getDocuments( String strIdDocument )
    {
        // There is no incremental indexation
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getResourceUid( String strResourceId, String strResourceType )
    {
        // There is no incremental indexation
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getResourcesName(  )
    {
        // There is no incremental indexation
        return null;
    }
}
