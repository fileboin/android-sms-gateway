import com.smspeer.app.modules.health.HealthService
import com.smspeer.app.modules.health.monitors.BatteryMonitor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val healthModule = module {
    singleOf(::BatteryMonitor)
    singleOf(::HealthService)
}