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

public class InAppSell {
    BillingClient billingClient;
    boolean connected = false,verifyConsumed = false,verifySub = false,isSub = false;
    List<ProductDetails> productDetailsList = new ArrayList<>();
    List<ProductDetails> productDetailsSubList = new ArrayList<>();

    public static void test(){

    }

   public boolean connectToGooglePlay() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                connected = false;
                connectToGooglePlay();
            }
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    connected = true;
                }
            }
        });
        return connected;
    }

    @SuppressLint("SetTextI18n")
   public List<ProductDetails> getInAppProducts(ImmutableList<QueryProductDetailsParams.Product> productList) {
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
   public List<ProductDetails> getSubProducts(ImmutableList<QueryProductDetailsParams.Product> productList) {
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

    public void launchPurchaseFlow(ProductDetails productDetails, Activity activity) {
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

    public boolean verifyConsumablePurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        ConsumeResponseListener listener = (billingResult, s) -> {
            verifyConsumed = billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK;
        };
        billingClient.consumeAsync(consumeParams, listener);
        return  verifyConsumed;
    }

    public boolean verifySubPurchase(Purchase purchases) {
        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchases.getPurchaseToken())
                .build();
        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
            verifySub = billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK;
        });
      return  verifySub;
    }

    public boolean checkSubscription(Context context){
        billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener((billingResult, list) -> {}).build();
        final BillingClient finalBillingClient = billingClient;
        if(connectToGooglePlay()){
            finalBillingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), (billingResult1, list) -> {
                        if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK){
                            isSub = list.size() > 0;
                        }
                    });
        }
        return  isSub;
    }
}
