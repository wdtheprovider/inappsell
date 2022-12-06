package com.wdtheprovider.inapppurchase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class InAppSell {

   public static boolean connectToGooglePlay(BillingClient billingClient) {
       final boolean[] connected = {false};
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                connected[0] = false;
                connectToGooglePlay(billingClient);
            }
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    connected[0] = true;
                }
            }
        });
        return connected[0];
    }

    @SuppressLint("SetTextI18n")
   public static List<ProductDetails> getInAppProducts(BillingClient billingClient, ImmutableList<QueryProductDetailsParams.Product> productList) {
        List<ProductDetails> productDetailsList = new ArrayList<>();
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();
        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            //Clear the list
            productDetailsList.clear();
            productDetailsList.addAll(list);
        });
        return productDetailsList;
    }

    @SuppressLint("SetTextI18n")
   public static List<ProductDetails> getSubProducts(BillingClient billingClient, ImmutableList<QueryProductDetailsParams.Product> productList) {
        List<ProductDetails> productDetailsSubList = new ArrayList<>();
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();
        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            //Clear the list
            productDetailsSubList.clear();
            productDetailsSubList.addAll(list);
        });
        return productDetailsSubList;
    }

    public static void launchPurchaseFlow(BillingClient billingClient, ProductDetails productDetails, Activity activity) {
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();
        billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    public static boolean verifyConsumablePurchase(BillingClient billingClient, Purchase purchase) {
        AtomicBoolean verifyConsumed = new AtomicBoolean(false);
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        ConsumeResponseListener listener = (billingResult, s) -> {
            verifyConsumed.set(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK);
        };
        billingClient.consumeAsync(consumeParams, listener);
        return verifyConsumed.get();
    }

    public boolean verifySubPurchase(BillingClient billingClient, Purchase purchases) {
       AtomicBoolean verifySub = new AtomicBoolean(false);
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchases.getPurchaseToken())
                .build();
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
            verifySub.set(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK);
        });
      return verifySub.get();
    }

    public boolean checkSubscription(BillingClient billingClient, Context context){
        AtomicBoolean isSub = new AtomicBoolean(false);
        billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener((billingResult, list) -> {}).build();
        final BillingClient finalBillingClient = billingClient;
        if(connectToGooglePlay(billingClient)){
            finalBillingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), (billingResult1, list) -> {
                        if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK){
                            isSub.set(list.size() > 0);
                        }
                    });
        }
        return isSub.get();
    }
}
