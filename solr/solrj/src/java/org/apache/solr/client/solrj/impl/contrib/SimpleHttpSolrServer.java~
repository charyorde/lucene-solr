/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opevel.server.integration.solr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;

/**
 *
 * @author Kayode Odeyemi
 * 
 * Code originally provided by Noble Paul
 * Patched by Kayode Odeyemi
 * @see https://issues.apache.org/jira/browse/SOLR-599
 */
public class SimpleHttpSolrServer extends SolrServer {

    private ResponseParser _parser;
    private String baseURL;
    private RequestWriter requestWriter = new RequestWriter();
    
    protected ModifiableSolrParams _invariantParams;

    /**
    * Maximum number of retries to attempt in the event of transient
    * errors.  Default: 0 (no) retries. No more than 1 recommended.
    */
    private int _maxRetries = 0;
  
    public SimpleHttpSolrServer(String baseURL) {
        this(baseURL, new BinaryResponseParser());
    }
    
    public SimpleHttpSolrServer(String baseURL, ResponseParser parser) {
        try {
            this.baseURL = new URL(baseURL).toExternalForm();
        } catch(MalformedURLException e) {
            throw new RuntimeException(e);
        }
        if( baseURL.endsWith( "/" ) ) {
            baseURL = baseURL.substring(0, baseURL.length()-1);
        }
        if( baseURL.indexOf( '?' ) >=0 ) {
            throw new RuntimeException("Invalid base url for solrj.  The base URL must not contain parameters: "+
                    baseURL);
        }
        
        _parser = parser;
    }
    
    public void setParser(ResponseParser parser) {
        this._parser = parser;
    }
    
    public void setRequestWriter(RequestWriter requestWriter) {
        this.requestWriter = requestWriter;
    }
    
    
    @Override
    public NamedList<Object> request(SolrRequest request) throws SolrServerException, IOException {
        SolrParams params = request.getParams();
        System.out.println("Params = " + params);
        Collection<ContentStream> streams = requestWriter.getContentStreams(request);
        String path = requestWriter.getPath(request);
        if( path == null || !path.startsWith( "/" ) ) {
            path = "/select";
        }
        ResponseParser parser = request.getResponseParser();
        if(parser == null) parser = _parser;
        System.out.println("Parser is: " + parser);
        // The parser 'wt=' and 'version=' params are used instead of the original params
    /*ModifiableSolrParams wparams = new ModifiableSolrParams();
    wparams.set( CommonParams.WT, parser.getWriterType() );
    wparams.set( CommonParams.VERSION, parser.getVersion());
    if( params == null ) {
      params = wparams;
    }
    else {
      params = new DefaultSolrParams( wparams, params );
    }
    
    if( _invariantParams != null ) {
      params = new DefaultSolrParams( _invariantParams, params );
    }*/
        // Put params in encParams
        StringBuilder encParams = new StringBuilder();
        addParam(encParams, CommonParams.WT, parser.getWriterType());
        System.out.println("encParams= " + encParams);
        
        if(parser.getClass() != BinaryResponseParser.class ){
            addParam(encParams,CommonParams.VERSION, parser.getVersion());
        }
        if (params != null){
            Iterator<String> iter = params.getParameterNamesIterator();
            while(iter.hasNext()) {
                String key = iter.next();
                String[] sarr = params.getParams(key);
                if(sarr != null && sarr.length>0){
                    for(String val : sarr) {
                        addParam(encParams, key, val);
                    }
                }
            }
        }
        System.out.println("encParams after if params != null= " + encParams);
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        
        int tries = _maxRetries + 1;
        
        try {
            if(SolrRequest.METHOD.GET == request.getMethod()) {
                if(streams != null) throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "GET Can't send streams!");
                conn = (HttpURLConnection) new URL(baseURL+path+"?"+encParams.toString()).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(0);
            } else if(SolrRequest.METHOD.POST == request.getMethod()) {
                if(streams != null) {
                    conn = (HttpURLConnection) new URL(baseURL+path+"?"+encParams.toString()).openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(0);
                    os = conn.getOutputStream();
                    for(ContentStream stream : streams) {
                        if(stream instanceof RequestWriter.LazyContentStream) {
                            ((RequestWriter.LazyContentStream)stream).writeTo(os);
                        } else {
                            //TODO copy to the outputstream
                        }
                    }
                } else {
                    conn = (HttpURLConnection) new URL(baseURL+path+"?"+encParams.toString()).openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
                    conn.setConnectTimeout(0);
                    os = conn.getOutputStream();
                    os.write(encParams.toString().getBytes());
                }
            }
            int rc = conn.getResponseCode();
            is = conn.getInputStream();
            if(rc != 200) throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
                    "Invalid response code "+ rc);
            return parser.processResponse(is, "UTF-8");
        } finally {
            if(os != null) try { os.close(); } catch(Exception e) { }
            if(is != null) try { is.close();} catch (Exception e) { }
        }
    }

    private static void addParam(StringBuilder sb, String key, String param) {
        if(sb.length() > 0) sb.append("&");
        try {
            sb.append(key).append("=").append(URLEncoder.encode(param, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            // Must not happen
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Adds the documents supplied by the given iterator.
     * 
     * @param docIterator the iterator which returns SolrInputDocument instances
     * 
     * @return the response from the SolrServer
     * 
     * @throws SolrServerException
     * @throws IOException 
     */
    public UpdateResponse add(Iterator<SolrInputDocument> docIterator) 
            throws SolrServerException, IOException {
        UpdateRequest req = new UpdateRequest();
        req.setDocIterator(docIterator);
        return req.process(this);
    }
    
    public UpdateResponse addBeans(final Iterator<?> beanIterator) 
            throws SolrServerException, IOException {
        UpdateRequest req = new UpdateRequest();
        req.setDocIterator(new Iterator<SolrInputDocument>() {
            
            public boolean hasNext() {
                return beanIterator.hasNext();
            }

            @Override
            public SolrInputDocument next() {
                Object o = beanIterator.next();
                if(o == null) return null;
                return getBinder().toSolrInputDocument(o);
            }

            @Override
            public void remove() {
                beanIterator.remove();
            }
            
        });
        return req.process(this);
    }
    
}
