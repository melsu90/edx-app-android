package org.edx.mobile.view.custom.cache.offline;

import org.edx.mobile.view.custom.cache.WebResource;

/**
 * Created by Ryan
 * at 2019/9/27
 */
public interface ResourceInterceptor {

    WebResource load(Chain chain);

}
