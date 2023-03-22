package com.ab.forwarder.domain.bean

import com.ab.forwarder.domain.strategy.balancer.ForwarderStrategy

open class ForwarderConfiguration(
    open val aUrl: String,
    open val bUrl: String,
    open val discriminatorValues: Array<String>,
    open val forwarderStrategy: ForwarderStrategy.StrategyType,
    open val missingDiscriminatorStrategy: ForwarderStrategy.MissingHeaderStrategyType,
    open val discriminatorName: String,
    open val discriminatorType: ForwarderStrategy.HeaderType,
    open val discriminatorValuePath: String?= null,
    open val discriminatorLocation: ForwarderStrategy.DiscriminatorLocation
) {
}