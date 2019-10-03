package com.cs407.team15.redstone.ui.location;

import android.content.Context;

import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;
import java.io.InputStream;
@GlideModule
public class LocationAppGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry){
        registry.append(StorageReference.class, InputStream.class, new FirebaseImageLoader.Factory());
    }

}
