package org.edx.mobile.view.custom.cache;

import android.content.Context;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;

import org.edx.mobile.view.custom.cache.config.CacheConfig;
import org.edx.mobile.view.custom.cache.WebResource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

public interface OfflineServer {

    @NonNull
    WebResourceResponse get(CacheRequest request);
    
    @NonNull
    void addResourceInterceptor(ResourceInterceptor interceptor);

    @NonNull
    void destroy();
  
        @Singleton
        class Impl implements OfflineServer {
          
            private final Context context;
            private final CacheConfig mCacheConfig;
            private final WebResourceResponseGenerator mResourceResponseGenerator;
            private List<ResourceInterceptor> mBaseInterceptorList;
            private List<ResourceInterceptor> mForceModeChainList;
          
            @Inject
            public Impl(@ApplicationContext Context context,
                        CacheConfig cacheConfig) {
                this.context = context;
                this.mCacheConfig = cacheConfig;
                this.mResourceResponseGenerator = new DefaultWebResponseGenerator();
            }
          
            @NonNull
            @Override
            public WebResourceResponse get(CacheRequest request) {
//                boolean isForceMode = request.isForceMode();
                Context context = mContext;
                CacheConfig config = mCacheConfig;
//                List<ResourceInterceptor> interceptors = isForceMode ? buildForceModeChain(context, config) : buildDefaultModeChain(context);
                List<ResourceInterceptor> interceptors = buildForceModeChain(context, config);
                WebResource resource = callChain(interceptors, request);
                return mResourceResponseGenerator.generate(resource, request.getMime());
            }

            @NonNull
            @Override
            public synchronized void addResourceInterceptor(ResourceInterceptor interceptor) {
                if (mBaseInterceptorList == null) {
                    mBaseInterceptorList = new ArrayList<>();
                }
                mBaseInterceptorList.add(interceptor);
            }

            @NonNull
            @Override
            public synchronized void destroy() {
                //destroyAll(mDefaultModeChainList);
                destroyAll(mForceModeChainList);
            }
          
            private List<ResourceInterceptor> buildForceModeChain(Context context, CacheConfig cacheConfig) {
                if (mForceModeChainList == null) {
                    int interceptorsCount = 3 + getBaseInterceptorsCount();
                    List<ResourceInterceptor> interceptors = new ArrayList<>(interceptorsCount);
                    if (mBaseInterceptorList != null && !mBaseInterceptorList.isEmpty()) {
                        interceptors.addAll(mBaseInterceptorList);
                    }
                    interceptors.add(MemResourceInterceptor.getInstance(cacheConfig));
                    interceptors.add(new DiskResourceInterceptor(cacheConfig));
                    interceptors.add(new ForceRemoteResourceInterceptor(context, cacheConfig));
                    mForceModeChainList = interceptors;
                }
                return mForceModeChainList;
            }
          
            private WebResource callChain(List<ResourceInterceptor> interceptors, CacheRequest request) {
                Chain chain = new Chain(interceptors);
                return chain.process(request);
            }

            private void destroyAll(List<ResourceInterceptor> interceptors) {
                if (interceptors == null || interceptors.isEmpty()) {
                    return;
                }
                for (ResourceInterceptor interceptor : interceptors) {
                    if (interceptor instanceof Destroyable) {
                        ((Destroyable) interceptor).destroy();
                    }
                }
            }

            private int getBaseInterceptorsCount() {
                return mBaseInterceptorList == null ? 0 : mBaseInterceptorList.size();
            }
            
        }
}
