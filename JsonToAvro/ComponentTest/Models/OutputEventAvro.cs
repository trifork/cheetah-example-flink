// ------------------------------------------------------------------------------
// <auto-generated>
//    Generated by avrogen, version 1.11.2
//    Changes to this file may cause incorrect behavior and will be lost if code
//    is regenerated
// </auto-generated>
// ------------------------------------------------------------------------------
namespace cheetah.example.model.avrorecord
{
	using System;
	using System.Collections.Generic;
	using System.Text;
	using global::Avro;
	using global::Avro.Specific;
	
	[global::System.CodeDom.Compiler.GeneratedCodeAttribute("avrogen", "1.11.2")]
	public partial class OutputEventAvro : global::Avro.Specific.ISpecificRecord
	{
		public static global::Avro.Schema _SCHEMA = global::Avro.Schema.Parse("{\"type\":\"record\",\"name\":\"OutputEventAvro\",\"namespace\":\"cheetah.example.model.avro" +
				"record\",\"fields\":[{\"name\":\"deviceId\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"do" +
				"uble\"},{\"name\":\"timestamp\",\"type\":\"long\"}]}");
		private string _deviceId;
		private double _value;
		private long _timestamp;
		public virtual global::Avro.Schema Schema
		{
			get
			{
				return OutputEventAvro._SCHEMA;
			}
		}
		public string deviceId
		{
			get
			{
				return this._deviceId;
			}
			set
			{
				this._deviceId = value;
			}
		}
		public double value
		{
			get
			{
				return this._value;
			}
			set
			{
				this._value = value;
			}
		}
		public long timestamp
		{
			get
			{
				return this._timestamp;
			}
			set
			{
				this._timestamp = value;
			}
		}
		public virtual object Get(int fieldPos)
		{
			switch (fieldPos)
			{
			case 0: return this.deviceId;
			case 1: return this.value;
			case 2: return this.timestamp;
			default: throw new global::Avro.AvroRuntimeException("Bad index " + fieldPos + " in Get()");
			};
		}
		public virtual void Put(int fieldPos, object fieldValue)
		{
			switch (fieldPos)
			{
			case 0: this.deviceId = (System.String)fieldValue; break;
			case 1: this.value = (System.Double)fieldValue; break;
			case 2: this.timestamp = (System.Int64)fieldValue; break;
			default: throw new global::Avro.AvroRuntimeException("Bad index " + fieldPos + " in Put()");
			};
		}
	}
}
