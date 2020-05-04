package org.edumips64.client;

import org.edumips64.core.CPU;

/* Factory class to generate Result objects.
   Injects a representation of the CPU status in every Result object that's created. */
public class ResultFactory {
    private CPU cpu;
    public ResultFactory(CPU cpu) {
        this.cpu = cpu;
    }

    public Result Success() {
        Result r = new Result(true, "");
        r.status = WebUi.FromCpuStatus(cpu.getStatus());
        return r;
    }

    public Result Failure(String errorMessage) {
        Result r = new Result(false, errorMessage);
        r.status = WebUi.FromCpuStatus(cpu.getStatus());
        return r;
    }
}