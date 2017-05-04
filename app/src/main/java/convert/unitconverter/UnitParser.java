package convert.unitconverter;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class UnitParser {

    private final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readUnits(parser);
        } finally {
            in.close();
        }
    }

    private List readUnits(XmlPullParser parser) throws XmlPullParserException, IOException {
        List units = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "units");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("unit")) {
                units.add(readUnit(parser));
            } else {
                skip(parser);
            }
        }
        return units;
    }

    private Unit readUnit(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "unit");
        String unitName = null;
        String unitType = null;
        //Boolean metric = false;
        Double conversionFactor = null;
        Double conversionConstant = 0.0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name){
                case "name":
                    unitName = readUnitName(parser);
                    break;
                case "type":
                    unitType = readUnitType(parser);
                    break;
                /*case "metric":
                    metric = readMetric(parser);
                    break;*/
                case "conversionFactor":
                    conversionFactor = readConversionFactor(parser);
                    break;
                case "conversionConstant":
                    conversionConstant = readConversionConstant(parser);
                    break;
                default:
                    skip(parser);
            }
        }
        return new Unit(unitName, unitType, conversionFactor, conversionConstant);
    }

    // Processes title tags in the feed.
    private String readUnitName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return name;
    }

    private String readUnitType(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "type");
        String type = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "type");
        return type;
    }

    /*private Boolean readMetric(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "metric");
        String value = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "metric");
        return value.equalsIgnoreCase("true");
    }*/

    private Double readConversionFactor(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "conversionFactor");
        Double factor = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "conversionFactor");
        return factor;
    }

    private Double readConversionConstant(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "conversionConstant");
        Double factor = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "conversionConstant");
        return factor;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
