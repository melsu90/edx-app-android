package org.edx.mobile.view.custom.cache.offline;

import android.webkit.WebResourceResponse;

import org.edx.mobile.view.custom.cache.WebResource;

/**
 * Created by Ryan
 * at 2019/10/8
 */
public interface WebResourceResponseGenerator {

    WebResourceResponse generate(WebResource resource, String urlMime);

}
