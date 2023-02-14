import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.DataObject;

@Model("test.model")
public class SimpleElement {
    @FactoryMethod
    public SimpleElement(Data data) {

    }

    @DataObject
    public record Data(String data) {

    }
}