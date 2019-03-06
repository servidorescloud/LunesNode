package io.lunes.settings

import com.typesafe.config.Config
import io.lunes.metrics.Metrics
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

case class LunesSettings(directory: String,
                         dataDirectory: String,
                         maxCacheSize: Int,
                         networkSettings: NetworkSettings,
                         walletSettings: WalletSettings,
                         blockchainSettings: BlockchainSettings,
                         checkpointsSettings: CheckpointsSettings,
                         feesSettings: FeesSettings,
                         minerSettings: MinerSettings,
                         restAPISettings: RestAPISettings,
                         synchronizationSettings: SynchronizationSettings,
                         utxSettings: UtxSettings,
                         featuresSettings: FeaturesSettings,
                         metrics: Metrics.Settings)

object LunesSettings {

  import NetworkSettings.networkSettingsValueReader

  val configPath: String = "lunes"

  def fromConfig(config: Config): LunesSettings = {
    val directory = config.as[String](s"$configPath.directory")
    val dataDirectory = config.as[String](s"$configPath.data-directory")
    val maxCacheSize = config.as[Int](s"$configPath.max-cache-size")
    val networkSettings = config.as[NetworkSettings]("lunes.network")
    val walletSettings = config.as[WalletSettings]("lunes.wallet")
    val blockchainSettings = BlockchainSettings.fromConfig(config)
    val checkpointsSettings = CheckpointsSettings.fromConfig(config)
    val feesSettings = FeesSettings.fromConfig(config)
    val minerSettings = config.as[MinerSettings]("lunes.miner")
    val restAPISettings = RestAPISettings.fromConfig(config)
    val synchronizationSettings = SynchronizationSettings.fromConfig(config)
    val utxSettings = config.as[UtxSettings]("lunes.utx")
    val featuresSettings = config.as[FeaturesSettings]("lunes.features")
    val metrics = config.as[Metrics.Settings]("metrics")

    LunesSettings(
      directory,
      dataDirectory,
      maxCacheSize,
      networkSettings,
      walletSettings,
      blockchainSettings,
      checkpointsSettings,
      feesSettings,
      minerSettings,
      restAPISettings,
      synchronizationSettings,
      utxSettings,
      featuresSettings,
      metrics
    )
  }
}
