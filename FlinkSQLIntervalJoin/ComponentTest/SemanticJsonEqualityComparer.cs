using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;
using System.Text;
using System.Text.Json;
using System.Text.Json.Nodes;
using System.Threading.Tasks;

namespace FlinkStates.ComponentTest
{
    internal class SemanticJsonEqualityComparer
    {

        public bool Equal(JsonNode? left, JsonNode? right)
        {
            if (left == null || right == null)
            {
                return left == right;
            }
            else
            {
                return EqualSwitchByJsonValueKind(left, right);
            }
        }

        private bool EqualSwitchByJsonValueKind(JsonNode left, JsonNode right)
        {
            switch (left.GetValueKind())
            {
                case JsonValueKind.Object:
                    return EqualJsonObject(left.AsObject(), right);
                case JsonValueKind.Array:
                    return EqualJsonArray(left.AsArray(), right);
                case JsonValueKind.String:
                    return EqualStringValue(left.AsValue(), right);
                case JsonValueKind.Number:
                    return EqualNumberValue(left.AsValue(), right);
                default:
                    return left.GetValueKind() == right.GetValueKind();
            }
        }

  

        private bool EqualJsonObject(JsonObject left, JsonNode right)
        {
            if (!right.GetValueKind().Equals(JsonValueKind.Object))
            {
                return false;
            }

            var rightAsObject = right.AsObject();

            foreach (var leftElement in left)
            {
                if (!rightAsObject.ContainsKey(leftElement.Key) || !Equal(leftElement.Value, rightAsObject[leftElement.Key]))
                {
                    return false;
                }
            }

            return CheckSizeEqual(left, rightAsObject);
        }

        private bool CheckSizeEqual(JsonObject left, JsonObject right)
        {
            return right.Count == left.Count;
        }


        private bool EqualJsonArray(JsonArray left, JsonNode right)
        {
            if (!right.GetValueKind().Equals(JsonValueKind.Array))
            {
                return false;
            }

            var rightAsArray = right.AsArray();

            foreach (var leftElement in left)
            {
                if (!EqualAnyInArray(leftElement, rightAsArray))
                {
                    return false;
                }
            }

            return CheckSizeEqual(left, rightAsArray);
        }

        private bool EqualAnyInArray(JsonNode? left, JsonArray rightArray)
        {
            foreach (var rightElement in rightArray)
            {
                if (Equal(left, rightElement))
                {
                    return true;
                }
            }

            return false;
        }

        private bool CheckSizeEqual(JsonArray left, JsonArray right)
        {
            return right.Count == left.Count;
        }


        private bool EqualStringValue(JsonValue leftJsonValue, JsonNode right)
        {
            return right.GetValueKind() == JsonValueKind.String 
                && leftJsonValue.GetValue<string>().Equals(right.GetValue<string>());
        }

        // Possibly needs to support more data types
        private bool EqualNumberValue(JsonValue leftJsonValue, JsonNode right)
        {
            if (right.GetValueKind() != JsonValueKind.Number) { return false; }

            if (leftJsonValue.TryGetValue<double>(out double leftNumber) && leftJsonValue.TryGetValue<double>(out double rightNumber)) {
                return leftNumber.Equals(rightNumber);
            } else {
                return leftJsonValue.GetValue<long>().Equals(right.GetValue<long>());
            }
        }
    }
}
