import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.DataObject;

import java.util.*;

@Model("test.model")
public class ChaoticElement {
    @FactoryMethod
    public ChaoticElement(Data data) {

    }

    public record CustomData() {

    }

    @DataObject
    public record Data<T>(List<String> dataList, String[] dataArray, HashSet<String> dataSet,
            List<List<Set<Integer>>> messyList, Set nonParameterizedSet, T genericParameter,
            Map<String, List<String>> map, Map genericMap, Map<Object, String> objectToStringMap, CustomData customData,
            List<? extends String> wildcardList, int primitiveInt, Integer wrapperInt, byte primitiveByte,
            Byte wrapperByte, short primitiveShort, Short wrapperShort, char primitiveChar, Character wrapperChar,
            long primitiveLong, Long wrapperLong, Object object, List<?> wildcardListNoBounds, float primitiveFloat,
            Float wrapperFloat, double primitiveDouble, Double wrapperDouble, boolean primitiveBoolean,
            Boolean wrapperBoolean, Collection<CustomData> stringCollection) {

    }
}