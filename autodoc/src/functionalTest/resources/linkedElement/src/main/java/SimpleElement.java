import com.github.steanky.element.core.annotation.*;

@Model("test.model")
public class SimpleElement {
    @FactoryMethod
    public SimpleElement(@Child("other_element") OtherElement otherElement) {

    }

    @DataObject
    public record Data(@ChildPath("other_element") String otherElement) {

    }
}