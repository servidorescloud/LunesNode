package io.lunes.security.air

import io.lunes.state.{Blockchain, ByteStr}
import io.lunes.transaction.AssetId
import scorex.account.Address

//case class AssetNameIdMap(name: String, assetId:AssetId)
//todo: implement this
/**
  * Asset Map Lists
  * @param map Maps Asset Name to Object
  * @tparam A  Generic element to which map
  */
case class AssetMapList[A](var map: Map[AssetName, A])

/**
  * Retrieve Assets Information
  */
object RetrieveAssetInfo {

  val emptyAsset = ByteStr(Array.emptyByteArray)
  val emptyPublicKey :Array[Byte] = Array.emptyByteArray

  /**
    * Get a [[AssetMapList]] for the Blockchain given an [[Address]] object
    * @param blockchain
    * @param account
    * @return
    */
  def assetIdFrom(blockchain: Blockchain,
                  account: Address): AssetMapList[AssetId] =
    AssetMapList(
      blockchain
        .portfolio(account)
        .assets
        .keySet
        .map(asset => {
          blockchain.assetDescription(asset) match {
            case Some(x) =>
              (x.name.map(_.toChar).mkString, asset) // Convert Asset name to String
            case None => ("", emptyAsset)
          }
        })
        .toMap
    )

  def assetIdFrom(blockchain: Blockchain,
                  account: String): AssetMapList[AssetId] = {
    Address.fromString(account) match {
      case Right(acc) => RetrieveAssetInfo.assetIdFrom(blockchain, acc)
      case Left(_)    => AssetMapList[AssetId](Map.empty)
    }
  }

  /**
    * get a [[AssetMapList]] for the Blockchain relating Asset Name and Issuer
    * @param blockchain
    * @param account
    * @return
    */
  def assetIssuerFrom(blockchain: Blockchain,
                      account: Address): AssetMapList[PublicKey] =
    AssetMapList(
      blockchain
        .portfolio(account)
        .assets
        .keySet
        .map(asset => {
          blockchain.assetDescription(asset) match {
            case Some(x) => (x.name.map(_.toChar).mkString, x.issuer.publicKey)
            case None    => ("", emptyPublicKey)
          }
        })
        .toMap
    )
}

object listedAssest {
  var byName: Map[String, String] = Map.empty
}