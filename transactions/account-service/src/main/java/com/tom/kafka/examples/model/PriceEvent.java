package com.tom.kafka.examples.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEvent {

	private String id;

	private String stock;
	
	private long newPrice;
}
