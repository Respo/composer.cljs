
Composer
------

> Template generating tool for Respo.

### Specs

Workspace router:

```edn
{:name :home
        ; points to template id
 :data {:pointer "id"
        ; valid tab are: :editor, :mocks, :settings
        :tab :markup
        :focused-mock "id"}}
```

Preview router:

```edn
{:name :preview
 :data {:pointer "id"}}
```

### Workflow

https://github.com/Cumulo/cumulo-workflow

### License

MIT
