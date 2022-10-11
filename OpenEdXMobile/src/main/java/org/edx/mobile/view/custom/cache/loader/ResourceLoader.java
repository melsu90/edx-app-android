package org.edx.mobile.view.custom.cache.loader;

import org.edx.mobile.view.custom.cache.WebResource;

/**
 * Created by Ryan
 * 2018/2/7 下午7:53
 */
public interface ResourceLoader {

    WebResource getResource(SourceRequest request);

}



