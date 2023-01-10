package kth.init.bicyclesthlm.data;

import android.annotation.SuppressLint;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import kth.init.bicyclesthlm.model.BicycleObject;

public class ParseString {

    //Takes String response from GET request and parses into an ArrayList of bicyclePump
    public static ArrayList<BicycleObject> getParsedBicyclePumpList(String xmlObject) {

        ArrayList<BicycleObject> bicycleObjects = new ArrayList<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(new StringReader(xmlObject));
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("AttributeValue")) {
                    eventType = parser.next();
                    if (eventType == XmlPullParser.TEXT)
                        bicycleObjects.add(new BicycleObject(parser.getText()));
                }

                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return bicycleObjects;
    }

    //Takes String response from GET request and parses into an ArrayList of strings (linkIds)
    public static ArrayList<String> getParsedLinkIdsList(String xmlObject, String attributeName) {
        ArrayList<String> stringList = new ArrayList<>();
        int counter = 0;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(new StringReader(xmlObject));
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals(attributeName)) {
                    eventType = parser.next();
                    if (eventType == XmlPullParser.TEXT){
                        counter++;

                        if(counter%3 == 0){
                            stringList.add(parser.getText());
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return stringList;
    }

}
