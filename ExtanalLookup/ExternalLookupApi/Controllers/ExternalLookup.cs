using Microsoft.AspNetCore.Mvc;
using System.Text.Json.Nodes;
using Newtonsoft.Json;

namespace ExternalLookupAPI.Controllers;

[ApiController]
[Route("[controller]")]
public class ExternalLookupController : ControllerBase
{
    private readonly ILogger<ExternalLookupController> _logger;

    public ExternalLookupController(ILogger<ExternalLookupController> logger)
    {
        _logger = logger;
    }

    [HttpGet(Name = "ExternalLookup")]
    public string Post()
    {
        return "External-lookup";
    }
}
