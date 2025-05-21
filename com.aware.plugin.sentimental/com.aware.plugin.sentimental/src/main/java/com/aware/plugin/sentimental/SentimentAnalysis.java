package com.aware.plugin.sentimental;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SentimentAnalysis {
	//confirm that context is needed
    private Context context;
    private JSONObject Dictionary;
    //store score as hashmap of categories
    private HashMap CalcScore=new HashMap<String,Double>();   ;

    public SentimentAnalysis(Context c) {
        context = c;
        //read sentiment dictionary from assets
        try {
		    Dictionary = new JSONObject(loadJSONFromAsset(c));
            JSONArray CatArr = Dictionary.getJSONArray("Categories");
            for(int i=0;i<CatArr.length();i++){
                String CatVal=CatArr.get(i).toString();
                Log.i("ABTest","In build category is "+CatVal);
                //CalcScore.put(CatVal,0);
                Double initVal=0.00;
                if (CatVal!=null){
                    CalcScore.put(CatVal,initVal);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();

        }
    }
	public void resetScore(){
        //method to reset score when package is swicthed
        // Getting an iterator
        Set<String> keys = CalcScore.keySet();
        for(String key: keys){
            CalcScore.replace(key,0.0);
        }



    }
	//function to read json file to string
	public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is;
            int id = context.getResources().getIdentifier("sentiment", "raw", context.getPackageName());
            is = context.getResources().openRawResource(id);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
    /**
     * Read the dictionary
     *
     * @return Dictionary as String
     */
    public String getDictionaryAsString() {
        
        return Dictionary.toString();
    }

    public boolean isMatch(String inpWord, String targetWord){
        //functiom that returns true if target word and inpword are identical or match based on wildcard
        if (targetWord.contains("*")){
            //do regexp compare of strings
            //String escapeMask=targetWord.replace(/[\-\[\]\/\{\}\(\)\+\?\.\\\^\$\|]/g, "\\$&");
            String regexString=targetWord.replace("*", ".*");
            boolean test=inpWord.matches("(?i:"+regexString+")");
            if (test){
                Log.i("ABTest","wildcard match inp is "+inpWord +" and tgt word is "+targetWord);
            }
            return test;
        } else {
            if (inpWord.equalsIgnoreCase(targetWord)){
                Log.i("ABTest","exact match inp is "+inpWord +" and tgt word is "+targetWord);
                return true;
            } else {
                return false;
            }
        }
    }
	public HashMap getScoreFromInput(String inpWord){
        double result;
        //JSONArray CatArr=Dictionary.getJSONArray("Categories");
        //process dictionary first
        String[] inpArr = inpWord.split("\\s+");
        try {
            JSONObject WordObj = Dictionary.getJSONObject("Words");
            Iterator wordList=WordObj.keys();
            while(wordList.hasNext()) {
                String wordItem = wordList.next().toString();
                //System.out.println("key is "+wordItem + " ");
                //Log.i("ABTest","word in dictionary is "+wordItem);
                JSONObject WordScores= WordObj.getJSONObject(wordItem);
                Iterator catScoreList=WordScores.keys();
                while(catScoreList.hasNext()) {
                    String catScoreItem = catScoreList.next().toString();
                    double catScore=WordScores.getDouble(catScoreItem);
                    for(int j=0; j<inpArr.length;j++){
                        String inpStr=inpArr[j];
                        if (isMatch(inpStr,wordItem)){
                            double currScore=Double.parseDouble(CalcScore.get(catScoreItem).toString());
                            double newScore=currScore+catScore;
                            CalcScore.replace(catScoreItem,newScore);
                        }
                    }
                }
            }

        } catch (JSONException ex){
            ex.printStackTrace();
        }

		return CalcScore;
	}

    public SentimentAnalysis getInstance(){
		return this;
	}
}
