package org.edx.mobile.view.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.custom.cache.FastOpenApi;
import org.edx.mobile.view.custom.cache.WebResource;
import org.edx.mobile.view.custom.cache.config.CacheConfig;
import org.edx.mobile.view.custom.cache.config.DefaultMimeTypeFilter;
import org.edx.mobile.view.custom.cache.config.FastCacheMode;
import org.edx.mobile.view.custom.cache.offline.Chain;
import org.edx.mobile.view.custom.cache.offline.ResourceInterceptor;

import java.io.File;

public class EdxWebView extends WebView implements FastOpenApi {

    private URLInterceptorWebViewClient mFastClient;

    @SuppressLint("SetJavaScriptEnabled")
    public EdxWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        CacheConfig config = new CacheConfig.Builder(context)
                .setCacheDir(context.getExternalCacheDir() + File.separator + "custom")
                .setExtensionFilter(new CustomMimeTypeFilter())
                .build();
        setCacheMode(FastCacheMode.FORCE, config);
        addResourceInterceptor(new ResourceInterceptor() {
            @Override
            public WebResource load(Chain chain) {
                return chain.process(chain.getRequest());
            }
        });
        final WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setUserAgentString(
                settings.getUserAgentString() + " " +
                        context.getString(R.string.app_name) + "/" +
                        BuildConfig.APPLICATION_ID + "/" +
                        BuildConfig.VERSION_NAME
        );
        int cacheMode = NetworkUtil.isConnected(context) ?
                WebSettings.LOAD_CACHE_ELSE_NETWORK : WebSettings.LOAD_CACHE_ELSE_NETWORK;
                //WebSettings.LOAD_DEFAULT : WebSettings.LOAD_CACHE_ELSE_NETWORK;
        settings.setCacheMode(cacheMode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            CookieManager cookieManager = CookieManager.getInstance();
//            cookieManager.setAcceptThirdPartyCookies(fastWebView, true);
//            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
//        }
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public class CustomMimeTypeFilter extends DefaultMimeTypeFilter {
        CustomMimeTypeFilter() {
            addMimeType("text/html");
        }
    }

    public void setCacheMode(FastCacheMode mode) {
        setCacheMode(mode, null);
    }

    @Override
    public void setCacheMode(FastCacheMode mode, CacheConfig cacheConfig) {
        mFastClient.setCacheMode(mode, cacheConfig);
        super.setWebViewClient(mFastClient);
    }

    public void addResourceInterceptor(ResourceInterceptor interceptor) {
        if (mFastClient != null) {
            mFastClient.addResourceInterceptor(interceptor);
        }
    }
}
