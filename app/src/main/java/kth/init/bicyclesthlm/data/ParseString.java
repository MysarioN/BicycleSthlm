package kth.init.bicyclesthlm.data;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import kth.init.bicyclesthlm.model.BicyclePump;

public class ParseString {

    //Takes String response from GET request and parses into an ArrayList of bicyclePump
    public static ArrayList<BicyclePump> getParsedBicyclePumpList(String xmlObject) {

        ArrayList<BicyclePump> bicyclePumps = new ArrayList<>();

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
                        bicyclePumps.add(new BicyclePump(parser.getText()));
                }

                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return bicyclePumps;
    }
}
