package se.enterprise.fikavault;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application root exposing API endpoints under /api/v1.
 */
@ApplicationPath("/api/v1")
public class RestApp extends Application {
}
