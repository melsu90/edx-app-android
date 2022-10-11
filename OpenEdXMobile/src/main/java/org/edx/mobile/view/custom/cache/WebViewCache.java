package org.edx.mobile.view.custom.cache;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.edx.mobile.view.custom.cache.offline.Destroyable;
import org.edx.mobile.view.custom.EdxWebView;
import org.edx.mobile.view.custom.cache.config.DefaultMimeTypeFilter;
import org.edx.mobile.view.custom.cache.config.CacheConfig;
import org.edx.mobile.view.custom.cache.config.FastCacheMode;
import org.edx.mobile.view.custom.cache.offline.CacheRequest;
import org.edx.mobile.view.custom.cache.offline.OfflineServer;
import org.edx.mobile.view.custom.cache.offline.OfflineServerImpl;
import org.edx.mobile.view.custom.cache.offline.ResourceInterceptor;
import org.edx.mobile.view.custom.cache.utils.MimeTypeMapUtils;

import java.io.File;
import java.util.Map;

import dagger.hilt.android.qualifiers.ApplicationContext;

public interface WebViewCache extends CacheApi, Destroyable {

  @NonNull
  WebResourceResponse getResource(WebResourceRequest request);
  
  @Singleton
    class Impl implements WebViewCache {
      
        private final Context context;
        private EdxWebView edxWebView;
      
      
        @Inject
        public Impl(@ApplicationContext Context context) {
            this.context = context;
        }
      
        @Override
        public WebResourceResponse getResource(WebResourceRequest webResourceRequest) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                String url = webResourceRequest.getUrl().toString();
                String extension = MimeTypeMapUtils.getFileExtensionFromUrl(url);
                String mimeType = MimeTypeMapUtils.getMimeTypeFromExtension(extension);
                String userAgent = edxWebView.getgetUserAgent();
                //TODO: check if this int declaration appropriate
                int cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK;
                CacheRequest cacheRequest = new CacheRequest();
                cacheRequest.setUrl(url);
                cacheRequest.setMime(mimeType);
                cacheRequest.setForceMode(mFastCacheMode == FastCacheMode.FORCE);
                cacheRequest.setUserAgent(userAgent);
                cacheRequest.setWebViewCacheMode(cacheMode);
                Map<String, String> headers = webResourceRequest.getRequestHeaders();
                cacheRequest.setHeaders(headers);
//                getOfflineServer().addResourceInterceptor(interceptor);
                return getOfflineServer().get(cacheRequest);
            }
            throw new IllegalStateException("an error occurred.");
        }
      
        @Override
        public void addResourceInterceptor(ResourceInterceptor interceptor) {
            getOfflineServer().addResourceInterceptor(interceptor);
        }
      
        private synchronized OfflineServer getOfflineServer() {
            if (mOfflineServer == null) {
                mOfflineServer = new OfflineServerImpl(mContext, getCacheConfig());
            }
            return mOfflineServer;
        }
      
        private CacheConfig getCacheConfig() {
            return mCacheConfig != null ? mCacheConfig : generateDefaultCacheConfig();
        }
      
        private CacheConfig generateDefaultCacheConfig() {
            return new CacheConfig.Builder(context)
                .setCacheDir(getExternalCacheDir() + File.separator + "authWebViewCache")
                .setExtensionFilter(new CustomMimeTypeFilter())
                .build();
        }
      
        public class CustomMimeTypeFilter extends DefaultMimeTypeFilter {
            CustomMimeTypeFilter() {
                addMimeType("text/html");
            }
        }

        @Override
        public void destroy() {
            if (mOfflineServer != null) {
                mOfflineServer.destroy();
            }
            // help gc
            mCacheConfig = null;
            mOfflineServer = null;
            mContext = null;
        }
    }

}
