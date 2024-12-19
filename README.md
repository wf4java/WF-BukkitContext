# WF-BukkitContext:
## Maven:
`Java(min): 17`
```xml
  <dependency>
    <groupId>io.github.wf4java</groupId>
    <artifactId>WF-BukkitContext</artifactId>
    <version>1.0.1</version>
  </dependency>
```

## Examples:

### Main
```java
@Getter
@BukkitConfiguration(
        enableConfig = true,
        enableCommand = true,
        enableMenu = true
)
public final class LiteAuction extends JavaPlugin {

    @Autowired
    private BukkitContext bukkitContext ;

    @SneakyThrows
    @Override
    public void onEnable() {
        BukkitContext.run(this);
    }

    @Override
    public void onDisable() {
        if(bukkitContext != null)
            bukkitContext.getBean(ConfigManager.class).getYml().saveAll();
    }

}
```
Ставим над Маин классом аннотацию BukkitConfiguration, и настраивает как вам нужно, в onEnable запускаем контекст, это основа!



### Context

Все классы над которым стоит @Component или @Config сохраняются в контейнер, и этот контейнер сам вставляет инстансы в нужные поля, где вы написали @Autowired, пример:

```java
@Component // For add this class to context
public class AuctionManager {

    @Autowired
    private static AuctionConfig auctionConfig;
    @Autowired
    private static MessageConfig messageConfig;
    
    
    //Methods
}


public class OpenAuctionExecutor extends Executor {
    
    
    // You can write to field from context
    @Autowired
    private static DefaultMenuManager defaultMenuManager;
    
    // Code
}


```



### Config file

```java
@Setter
@Getter
@Config(configName = "shop")
@SuppressWarnings("FieldMayBeFinal")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShopConfig extends AbstractConfig {

    private List<Material> blackList = List.of(Material.STONE, Material.GOLD_ORE);

    private Message successfulBuyMessage = new Message("<yellow>You successful buy this item!");

    private Map<UUID, Double> playerMoney = new HashMap<>();
    
    // Other fields...
    
}
```
Тут таким образом можно сохранять почти любые поля, если установить просто через Set, то оно само сохраниться в конфиге


### Commands

```java
@Component
public class CommandHandler {
    
    @Init
    private void init() {
        new Command("auction")
                .arguments(
                        new ArgumentSet(
                                new OpenAuctionExecutor()
                        )
                )
                .subCommands(
                        new Command("sell")
                                .arguments(
                                        new ArgumentSet(
                                                new SellItemExecutor(),
                                                new DoubleArg().setMin(0.1).setMax(2_000_000)
                                        )
                                )
                )
                .register();
    }
}

public class SellItemExecutor extends Executor {
    
    @Override
    public void executeForPlayer() throws MessageReturn, TargetableMessageReturn {
        auctionManager.sellItem(player(), argD(0));
    }
    
}
```
Тут все базово, думаю все всё поймут!



