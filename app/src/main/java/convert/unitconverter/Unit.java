package convert.unitconverter;

/**
 * Created by student on 7/23/2015.
 */
public class Unit {
    public final String name;
    public final String type;
    //public final Boolean metric;
    public final Double conversionFactor;
    public final Double conversionConstant;
    public static Integer sigFigs = 10;

    public Unit(String name, String type, Double conversionFactor){
        this.name = name;
        this.type = type;
        //this.metric = metric;
        this.conversionFactor = conversionFactor;
        this.conversionConstant = 0.0;
    }

    public Unit(String name, String type, Double conversionFactor, Double conversionConstant){
        this.name = name;
        this.type = type;
        //this.metric = metric;
        this.conversionFactor = conversionFactor;
        this.conversionConstant = conversionConstant;
    }

    public String toString(){
        return super.toString() + "; Name: " + name + "; Type: " + type + "; conversionFactor: " + conversionFactor;
    }

    public Double convert(Double value, Unit other){
        Double result;
        result = value*this.conversionFactor + this.conversionConstant; //convert to SI
        result = (result - other.conversionConstant)/other.conversionFactor; //convert to final value
        return result;
    }

    public static Double convert(Double value, Unit self, Unit other){
        Double result;
        result = value*self.conversionFactor + self.conversionConstant; //convert to SI
        result = (result - other.conversionConstant)/other.conversionFactor; //convert to final value
        return result;
    }

    public static Double convert(Double value, Double conversionFactor1, Double conversionFactor2, Double conversionConstant1, Double conversionConstant2){

        Double result;
        result = value*conversionFactor1 + conversionConstant1; //convert to SI
        result = (result - conversionConstant2)/conversionFactor2; //convert to final value
        return result;

    }

}