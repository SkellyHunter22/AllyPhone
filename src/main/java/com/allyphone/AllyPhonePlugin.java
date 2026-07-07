package com.allyphone;

import com.allyphone.api.AlsBankingService;
import com.allyphone.api.AppRegistry;
import com.allyphone.api.BankingService;
import com.allyphone.api.CompositeBankingService;
import com.allyphone.api.VaultBankingService;
import com.allyphone.apps.AdminApp;
import com.allyphone.apps.AlertsApp;
import com.allyphone.apps.AppStoreApp;
import com.allyphone.apps.ArcadeApp;
import com.allyphone.apps.CasinoApp;
import com.allyphone.apps.CustomizeApp;
import com.allyphone.apps.EightBallApp;
import com.allyphone.apps.ExtrasApp;
import com.allyphone.apps.FriendsApp;
import com.allyphone.apps.GpsApp;
import com.allyphone.apps.HelpApp;
import com.allyphone.apps.InstalledAppsStore;
import com.allyphone.apps.JobsApp;
import com.allyphone.apps.MessagesApp;
import com.allyphone.apps.MusicApp;
import com.allyphone.apps.NewsApp;
import com.allyphone.apps.PetsApp;
import com.allyphone.apps.PlotsApp;
import com.allyphone.apps.QuestsApp;
import com.allyphone.apps.ServersApp;
import com.allyphone.apps.TowerManagerApp;
import com.allyphone.apps.TownyApp;
import com.allyphone.apps.WalletApp;
import com.allyphone.apps.WeatherApp;
import com.allyphone.commands.AtmCommand;
import com.allyphone.commands.CellTowerCommand;
import com.allyphone.commands.PhoneAlertCommand;
import com.allyphone.commands.PhoneCommand;
import com.allyphone.commands.PhoneNewsCommand;
import com.allyphone.commands.SmsCommand;
import com.allyphone.discord.DiscordWebhook;
import com.allyphone.listeners.GuiClickListener;
import com.allyphone.listeners.JoinListener;
import com.allyphone.listeners.PendingInputChatListener;
import com.allyphone.listeners.RespawnListener;
import com.allyphone.listeners.RightClickListener;
import com.allyphone.listeners.SmsChatListener;
import com.allyphone.listeners.TowerPatternListener;
import com.allyphone.map.TowerMapIntegration;
import com.allyphone.papi.AllyPhoneExpansion;
import com.allyphone.service.AlertService;
import com.allyphone.service.AtmStore;
import com.allyphone.service.BillingService;
import com.allyphone.service.CellTowerStore;
import com.allyphone.service.CellTowerVisualizer;
import com.allyphone.service.MessageService;
import com.allyphone.service.MonthlyBillingTask;
import com.allyphone.service.NewsService;
import com.allyphone.service.PendingInputService;
import com.allyphone.service.PendingSmsService;
import com.allyphone.service.PhoneCustomizationStore;
import com.allyphone.service.PhoneService;
import com.allyphone.service.PluginLogService;
import com.allyphone.service.ResourcePackHost;
import com.allyphone.service.ServicePlanService;
import com.allyphone.service.SignalDebugTask;
import com.allyphone.service.SignalService;
import com.allyphone.service.StatusBarTask;
import com.allyphone.sql.AlertSQLService;
import com.allyphone.sql.Database;
import com.allyphone.sql.MessageSQLService;
import com.allyphone.sql.NewsSQLService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;

public class AllyPhonePlugin extends JavaPlugin {

    private static AllyPhonePlugin instance;

    private Database database;
    private MessageSQLService messageSqlService;
    private NewsSQLService newsSqlService;
    private AlertSQLService alertSqlService;

    private ServicePlanService servicePlanService;
    private CellTowerStore cellTowerStore;
    private AtmStore atmStore;
    private InstalledAppsStore installedAppsStore;
    private AppRegistry appRegistry;

    private PhoneService phoneService;
    private BillingService billingService;
    private SignalService signalService;
    private MessageService messageService;
    private NewsService newsService;
    private AlertService alertService;
    private DiscordWebhook discordWebhook;
    private BankingService bankingService;
    private PendingSmsService pendingSmsService;
    private PendingInputService pendingInputService;
    private PhoneCustomizationStore phoneCustomizationStore;
    private TowerMapIntegration towerMapIntegration;
    private CellTowerVisualizer cellTowerVisualizer;
    private ResourcePackHost resourcePackHost;

    private BukkitTask statusBarTask;
    private BukkitTask billingTask;
    private BukkitTask signalDebugTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        PluginLogService pluginLogService = new PluginLogService(this);
        pluginLogService.printEnableArt();
        pluginLogService.recordEnable();

        database = new Database(this);
        try {
            database.init();
        } catch (SQLException e) {
            getLogger().severe("Failed to initialize database, disabling AllyPhone: " + e.getMessage());
            pluginLogService.logError("Database init failed", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        messageSqlService = new MessageSQLService(database);
        newsSqlService = new NewsSQLService(database);
        alertSqlService = new AlertSQLService(database);
        servicePlanService = new ServicePlanService(database);
        cellTowerStore = new CellTowerStore(database);
        atmStore = new AtmStore(database);
        installedAppsStore = new InstalledAppsStore(database);
        phoneCustomizationStore = new PhoneCustomizationStore(database);
        discordWebhook = new DiscordWebhook(this);

        phoneService = new PhoneService();
        billingService = new BillingService(this);
        signalService = new SignalService(this);
        messageService = new MessageService(this, messageSqlService);
        newsService = new NewsService(this, newsSqlService);
        alertService = new AlertService(this, alertSqlService);
        pendingSmsService = new PendingSmsService();
        pendingInputService = new PendingInputService();
        towerMapIntegration = new TowerMapIntegration(this);
        cellTowerVisualizer = new CellTowerVisualizer(this);
        resourcePackHost = new ResourcePackHost(this);
        resourcePackHost.start();
        com.allyphone.bedrock.GeyserBridge.register(this);

        setupBanking();
        setupApps();
        registerListeners();
        registerCommands();
        setupPlaceholderApi();
        scheduleTasks();
        towerMapIntegration.register();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getLogger().info("AllyPhone enabled.");
    }

    @Override
    public void onDisable() {
        if (statusBarTask != null) statusBarTask.cancel();
        if (billingTask != null) billingTask.cancel();
        if (signalDebugTask != null) signalDebugTask.cancel();
        if (cellTowerVisualizer != null) cellTowerVisualizer.shutdown();
        if (resourcePackHost != null) resourcePackHost.stop();
        if (database != null) database.close();
        getLogger().info("AllyPhone disabled.");
    }

    private void setupBanking() {
        BankingService vault = null;
        BankingService alsBanker = null;

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
            if (provider != null) {
                vault = new VaultBankingService(provider.getProvider());
            }
        }
        if (getServer().getPluginManager().isPluginEnabled("AlsBanker")) {
            try {
                alsBanker = new AlsBankingService();
            } catch (IllegalStateException e) {
                getLogger().warning("AlsBanker is enabled but its API is unavailable: " + e.getMessage());
            }
        }

        // Vault is the balance of record (it's what economy placeholders reflect); AlsBanker,
        // when present, only contributes loan summary + transaction history on top of it.
        if (vault != null) {
            bankingService = (alsBanker != null) ? new CompositeBankingService(vault, alsBanker) : vault;
            getLogger().info("Using Vault for phone balance" + (alsBanker != null ? " (with AlsBanker loans/history)." : "."));
        } else if (alsBanker != null) {
            bankingService = alsBanker;
            getLogger().info("Using AlsBanker for phone billing.");
        } else {
            getLogger().warning("No banking plugin found (Vault/AlsBanker) - billing will be simulated only.");
        }
    }

    private void setupApps() {
        appRegistry = new AppRegistry();
        appRegistry.registerApp(new WalletApp());
        appRegistry.registerApp(new MessagesApp());
        appRegistry.registerApp(new NewsApp());
        appRegistry.registerApp(new WeatherApp());
        appRegistry.registerApp(new AlertsApp());
        appRegistry.registerApp(new FriendsApp());
        appRegistry.registerApp(new AppStoreApp());
        appRegistry.registerApp(new ExtrasApp());
        appRegistry.registerApp(new ServersApp());
        appRegistry.registerApp(new PlotsApp());
        appRegistry.registerApp(new TownyApp());
        appRegistry.registerApp(new TowerManagerApp());
        appRegistry.registerApp(new JobsApp());
        appRegistry.registerApp(new PetsApp());
        appRegistry.registerApp(new QuestsApp());
        appRegistry.registerApp(new MusicApp());
        appRegistry.registerApp(new HelpApp());
        appRegistry.registerApp(new CustomizeApp());
        appRegistry.registerApp(new AdminApp());
        appRegistry.registerApp(new EightBallApp());
        appRegistry.registerApp(new ArcadeApp());
        appRegistry.registerApp(new GpsApp());
        if (getConfig().getBoolean("casino.enabled", true)) {
            appRegistry.registerApp(new CasinoApp());
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new RightClickListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiClickListener(this), this);
        getServer().getPluginManager().registerEvents(new TowerPatternListener(this), this);
        getServer().getPluginManager().registerEvents(new SmsChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PendingInputChatListener(this), this);
    }

    private void registerCommands() {
        getCommand("phone").setExecutor(new PhoneCommand(this));
        getCommand("sms").setExecutor(new SmsCommand(this));
        getCommand("phonenews").setExecutor(new PhoneNewsCommand(this));
        getCommand("celltower").setExecutor(new CellTowerCommand(this));
        getCommand("atm").setExecutor(new AtmCommand(this));
        getCommand("phonealert").setExecutor(new PhoneAlertCommand(this));
    }

    private void setupPlaceholderApi() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new AllyPhoneExpansion(this).register();
            getLogger().info("Registered PlaceholderAPI expansion.");
        }
    }

    private void scheduleTasks() {
        // Refreshed well under the ~3s the client keeps an action bar message on screen, so it
        // never visibly flashes off between resends (see StatusBarTask for details).
        statusBarTask = new StatusBarTask(this).runTaskTimer(this, 20L, 40L);
        billingTask = new MonthlyBillingTask(this).runTaskTimer(this, 20L * 60, 20L * 60 * 60);
        if (getConfig().getBoolean("debug.signal", false)) {
            signalDebugTask = new SignalDebugTask(this).runTaskTimer(this, 200L, 200L);
        }
    }

    // Static accessor used throughout the plugin
    public static AllyPhonePlugin get() {
        return instance;
    }

    public PhoneService getPhoneService() {
        return phoneService;
    }

    public BillingService getBillingService() {
        return billingService;
    }

    public SignalService getSignalService() {
        return signalService;
    }

    public ServicePlanService getServicePlanService() {
        return servicePlanService;
    }

    public CellTowerStore getCellTowerStore() {
        return cellTowerStore;
    }

    public AtmStore getAtmStore() {
        return atmStore;
    }

    public InstalledAppsStore getInstalledAppsStore() {
        return installedAppsStore;
    }

    public AppRegistry getAppRegistry() {
        return appRegistry;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public NewsService getNewsService() {
        return newsService;
    }

    public MessageSQLService getMessageSqlService() {
        return messageSqlService;
    }

    public NewsSQLService getNewsSqlService() {
        return newsSqlService;
    }

    public AlertService getAlertService() {
        return alertService;
    }

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    public BankingService getBankingService() {
        return bankingService;
    }

    public PendingSmsService getPendingSmsService() {
        return pendingSmsService;
    }

    public PendingInputService getPendingInputService() {
        return pendingInputService;
    }

    public PhoneCustomizationStore getPhoneCustomizationStore() {
        return phoneCustomizationStore;
    }

    public TowerMapIntegration getTowerMapIntegration() {
        return towerMapIntegration;
    }

    public CellTowerVisualizer getCellTowerVisualizer() {
        return cellTowerVisualizer;
    }

    public ResourcePackHost getResourcePackHost() {
        return resourcePackHost;
    }

    /**
     * Re-reads config.yml and re-applies everything driven by it in-place (resource pack host,
     * banking backend, which apps are registered) without touching listeners, commands, tasks,
     * or the plugin's loaded code. Safe to call on a live server; does NOT pick up a replaced jar
     * file (see PhoneCommand's "full" reload, which hands that off to PlugManX).
     */
    public void reloadPluginState() {
        reloadConfig();

        if (resourcePackHost != null) {
            resourcePackHost.stop();
            resourcePackHost = new ResourcePackHost(this);
            resourcePackHost.start();
        }

        setupBanking();
        setupApps();
    }
}
