package com.cbibhor.eme.expensemanagereconomizer;

import android.os.Bundle;
import java.util.*;

import static java.lang.Math.max;

/**
 * Created by Bibhor Chauhan on 03-05-2017.
 */

public class FilterUtility {

    public String[] senderDict = {"AMAZON","Amazon","PAYTM","Paytm","WAYSMS","FCHRGE","ATMSBI"};
    public String[] keywordsAmazon = {"INR"};
    public String[] keywordsPaytm = {"Paid Rs.","Paid","paid","payment of"};
    public String[] keywordsFreecharge = {"paid"};
    public String[] keywordsRecharge = {"Recharge","recharge","Rchrg","successful"};
    public String[] keywordsATMSBI = {"Thank you for using","purchase","on POS","at","txn#"};
    public String[] keywordsATMSBIRecharge = {"BSNL","bsnl","Bsnl","IDEA","idea","Idea",
                                                "AIRTEL","airtel","Airtel","VODAFONE","vodafone","Vodafone",
                                                "DOCOMO","dococmo","Docomo"};
    public String[] extractionKeywords = {"INR","Rs.","Rs"};

    public String[] offersSenderDict = {"DOMINO","MYNTRA","PANTLS","BIGBZR","BMSHOW","BFCTRY","LNKART","OLACBS","PZAHUT"};
    public String[] offerSenderNames = {"DOMINOS","MYNTRA","PANTALOONS","BIG BAZAR","BOOK MY SHOW","BRAND FACTORY","LENSKART","OLA CABS","PIZZA HUT"};
    public String[] keywordsOffers = {"Discounts","discounts","DISCOUNTS","Discount","discount","DISCOUNT",
                                        "Offers","offers","OFFERS","Offer","offer","OFFER",
                                        "Sale","sale","SALE","Off","off","OFF",
                                        "Cashback","cashback","CASHBACK"};
    public String[] INkeyowrds = {"Amazon"};

    boolean isRecharge;

    int [] pre_kmp(String pat){
        int m=pat.length();
        int lps[] = new int[m];
        int i=1;
        int len=0;
        lps[0]=0;
        while(i<m){
            if(pat.charAt(i)==pat.charAt(len)) {
                len++; lps[i] = len; i++;
            }
            else{
                if(len!=0)	len=lps[len-1];
                else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    private Vector kmp(String pat, String txt){
        int lps[] = pre_kmp(pat);
        Vector index = new Vector(10);
        int i=0, j=0, k=0;
        int n=txt.length(), m=pat.length();
        while(i<n){
            if(txt.charAt(i)==pat.charAt(j)) {
                i++;
                j++;
            }
            if(j==m){
                index.addElement(i-j);
                //return true;
                k++;
                j=lps[j-1];
            }
            else if(i<n && txt.charAt(i)!=pat.charAt(j)){
                if(j!=0)	j=lps[j-1];
                else	i++;
            }
        }
        //return false;
        return index;
    }

    public Bundle foundSender(String sender, String message){
        Vector senderindex;
        isRecharge = false;
        for(int i=0; i<senderDict.length; i++) {
            senderindex = kmp(senderDict[i], sender);
            if (senderindex.size() != 0){
                if(i==0 || i==1)        return utilityAmazon(i,message);
                else if(i==2 || i==3)   return utilityPaytm(i,message);
                //else if(i==4)           return utilityPaytm(i,message);
                else if(i==5)           return utilityFreecharge(i,message);
                else if(i==6)           return utilityATMSBI(i,message);
            }
        }
        if(!isRecharge) return utilityRecharge(message);
        return null;
    }

    private Bundle utilityAmazon(int i, String message){
        int chk,amount=0;
        Bundle bundle = new Bundle();
        chk = checkKeywordsInMessage(keywordsAmazon,message);
        if(chk!=-1)
            amount = extractAmount(extractionKeywords, 0, message);
        if(amount!=0){
            bundle.putString("sender", senderDict[i]);
            bundle.putInt("message", amount);
            return bundle;
        }
        return null;
    }

    private Bundle utilityPaytm(int i, String message){
        int chk, amount=0;
        Bundle bundle = new Bundle();
        chk = checkKeywordsInMessage(keywordsPaytm,message);
        if(chk!=-1)
            amount = extractAmount(extractionKeywords, 1, message);
                    /*else{
                        if(checkMultipleKeywords(keywordsRecharge[0],keywordsRecharge[1],message))
                            amount = extractAmount(extractionKeywords, 1, message);
                    }*/
        if(amount!=0){
            bundle.putString("sender", senderDict[i]);
            bundle.putInt("message", amount);
            return bundle;
        }
        return null;
    }

    private Bundle utilityFreecharge(int i, String message){
        int chk, amount=0;
        Bundle bundle = new Bundle();
        chk = checkKeywordsInMessage(keywordsFreecharge,message);
        if(chk!=-1)
            amount = extractAmount(extractionKeywords,2,message);
                    /*else{
                        if(checkMultipleKeywords(keywordsRecharge[0],keywordsRecharge[1],message))
                            amount = extractAmount(extractionKeywords,2,message);
                    }*/
        if(amount!=0){
            bundle.putString("sender", senderDict[i]);
            bundle.putInt("message", amount);
            return bundle;
        }
        return null;
    }

    private Bundle utilityATMSBI(int i, String message){
        int amount=0;
        Bundle bundle = new Bundle();
        if(checkMultipleKeywords(keywordsATMSBI[0],keywordsATMSBI[1],message)){
            Vector mIndex, atIndex, txnIndex, sIndex;
            String mSender="";
            mIndex = kmp(keywordsATMSBI[2],message);
            atIndex = kmp(keywordsATMSBI[3],message);
            txnIndex = kmp(keywordsATMSBI[4],message);
            mSender = message.substring((Integer)atIndex.elementAt(0)+3, (Integer)txnIndex.elementAt(0));
            for(int k=0; k<keywordsATMSBIRecharge.length; k++){
                sIndex = kmp(keywordsATMSBIRecharge[k],mSender);
                if(sIndex.size()!=0){
                    isRecharge = true;
                    mSender = mSender + "Recharge";
                    break;
                }
            }
            amount = extractAmount(extractionKeywords,2,message);
            if(amount!=0){
                bundle.putString("sender", mSender);
                bundle.putInt("message", amount);
                return bundle;
            }
        }
        return null;
    }

    private Bundle utilityRecharge(String message){
        int amount = 0;
        Bundle bundle = new Bundle();
        if(checkMultipleKeywords(keywordsRecharge[0],keywordsRecharge[3],message)
                || checkMultipleKeywords(keywordsRecharge[1],keywordsRecharge[3],message)
                || checkMultipleKeywords(keywordsRecharge[2],keywordsRecharge[3],message)) {
            amount = extractAmount(extractionKeywords, 1, message);
            if(amount==0)
                amount = extractAmount(extractionKeywords, 2, message);
        }
        if (amount != 0) {
            bundle.putString("sender", "Recharge");
            bundle.putInt("message", amount);
            return bundle;
        }
        return null;
    }

    private Integer checkKeywordsInMessage(String[] keywords, String message){
        Vector mIndex;
        for(int k=0; k<keywords.length; k++){
            mIndex = kmp(keywords[k], message);
            if(mIndex.size()!=0)
                return k;
        }
        return -1;
    }

    private boolean checkMultipleKeywords(String keyword1, String keyword2, String message){
        Vector mIndex1, mIndex2;
        mIndex1 = kmp(keyword1, message);
        mIndex2 = kmp(keyword2, message);
        if(mIndex1.size()!=0 && mIndex2.size()!=0)  return true;
        return false;
    }

    private Integer extractAmount(String[] keyword, Integer chk, String message){
        Vector index;
        Integer amount=0;
        index = kmp(keyword[chk], message);
        if(index.size() != 0){
            //for(int k=0; k<index.size(); k++){
                int start = (Integer) index.elementAt(0) + keyword[chk].length();
                char ch = message.charAt(start);
                while(!Character.isDigit(ch)){
                    start++;
                    ch = message.charAt(start);
                }
                while(Character.isDigit(ch)){
                    amount = amount*10 + Character.getNumericValue(ch);
                    start++;
                    ch = message.charAt(start);
                }
                //if(amount!=0)   return amount;
            //}
        }
        return amount;
    }

    public Bundle isOffer(String sender, String message){
        Bundle offerBundle;
        Vector sIndex, mIndex;
        for(int i=0; i<offersSenderDict.length; i++){
            sIndex = kmp(offersSenderDict[i], sender);
            if(sIndex.size() != 0){
                if(i==8) {
                    offerBundle = new Bundle();
                    offerBundle.putString("sender",offerSenderNames[i]);
                    return offerBundle;
                }
                for(int j=0; j<keywordsOffers.length; j++){
                    mIndex = kmp(keywordsOffers[j], message);
                    if(mIndex.size() != 0){
                        offerBundle = new Bundle();
                        offerBundle.putString("sender",offerSenderNames[i]);
                        return offerBundle;
                    }
                    else{
                        offerBundle = checkInKeywords(message);
                        if(offerBundle!=null) return offerBundle;
                    }
                }
            }
        }
        return null;
    }

    private Bundle checkInKeywords(String message){
        Bundle chkbundle = new Bundle();
        Vector mIndex;
        for(int i=0; i<INkeyowrds.length; i++){
            mIndex = kmp(INkeyowrds[i], message);
            if(mIndex.size()!=0){
                if(i==0)    chkbundle.putString("sender","AMAZON");
                return chkbundle;
            }
        }
        return null;
    }

}